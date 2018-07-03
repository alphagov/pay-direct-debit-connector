package uk.gov.pay.directdebit.notifications.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.time.LocalDate;
import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class UserNotificationServiceTest {

    @Mock
    private AdminUsersClient mockAdminUsersClient;
    @Mock
    private DirectDebitConfig mockDirectDebitConfig;
    @Mock
    private LinksConfig mockLinksConfig;
    @Mock
    private UserNotificationService userNotificationService;

    private static final String EMAIL = "ksdfhkjsdh@sdjkfh.test";
    private PayerFixture payerFixture = aPayerFixture().withEmail(EMAIL);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withPayerFixture(
            payerFixture);

    private Transaction transaction = aTransactionFixture()
            .withMandateFixture(mandateFixture)
            .withAmount(12345L)
            .toEntity();

    @Before
    public void setUp() {
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);
        when(mockLinksConfig.getDirectDebitGuaranteeUrl()).thenReturn("https://frontend.url.test/direct-debit-guarantee");
    }

    @Test
    public void shouldSendMandateFailedEmail() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateFailedEmailFor(mandateFixture.toEntity());

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_FAILED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendOnDemandMandateCreatedEmail() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withPayerFixture(payerFixture)
                .toEntity();
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getMandateReference());
        emailPersonalisation.put("bank account last 2 digits", UserNotificationService.BANK_ACCOUNT_MASK_PREFIX + mandate.getPayer().getAccountNumberLastTwoDigits());
        emailPersonalisation.put("statement name", UserNotificationService.PLACEHOLDER_STATEMENT_NAME);
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendOnDemandMandateCreatedEmailFor(mandate);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.ON_DEMAND_MANDATE_CREATED, mandate, emailPersonalisation);
    }

    @Test
    public void shouldSendMandateCancelledEmail() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateCancelledEmailFor(mandateFixture.toEntity());

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_CANCELLED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendOneOffPaymentConfirmedEmail() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);

        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("amount", "123.45");
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference());
        emailPersonalisation.put("collection date", "21/05/2018");
        emailPersonalisation.put("bank account last 2 digits", UserNotificationService.BANK_ACCOUNT_MASK_PREFIX + payerFixture.getAccountNumberLastTwoDigits());
        emailPersonalisation.put("statement name", "THE-CAKE-IS-A-LIE");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendOneOffPaymentConfirmedEmailFor(transaction, LocalDate.parse("2018-05-21"));

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.ONE_OFF_PAYMENT_CONFIRMED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendOnDemandPaymentConfirmedEmail() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);

        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("amount", "123.45");
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference());
        emailPersonalisation.put("collection date", "21/05/2018");
        emailPersonalisation.put("bank account last 2 digits", UserNotificationService.BANK_ACCOUNT_MASK_PREFIX + payerFixture.getAccountNumberLastTwoDigits());
        emailPersonalisation.put("statement name", "THE-CAKE-IS-A-LIE");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendOnDemandPaymentConfirmedEmailFor(transaction, LocalDate.parse("2018-05-21"));

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.ON_DEMAND_PAYMENT_CONFIRMED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendPaymentFailedEmail() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendPaymentFailedEmailFor(transaction);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.PAYMENT_FAILED, mandateFixture.toEntity(), emailPersonalisation);
    }

}
