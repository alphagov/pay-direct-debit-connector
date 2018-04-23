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
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.model.Payer;
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
    private Payer payer = aPayerFixture().withEmail(EMAIL).toEntity();
    private Mandate mandate = MandateFixture.aMandateFixture().toEntity();

    private Transaction transaction = aTransactionFixture()
            .withAmount(12345L)
            .toEntity();

    @Before
    public void setUp() {
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);
        when(mockLinksConfig.getDirectDebitGuaranteeUrl()).thenReturn("https://frontend.url.test/direct-debit-guarantee");
    }

    @Test
    public void shouldSendMandateFailedEmailIfEmailNotifyIsEnabled() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getReference());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateFailedEmailFor(transaction, mandate, payer);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_FAILED, transaction, EMAIL, emailPersonalisation);
    }

    @Test
    public void shouldSendMandateCancelledEmailIfEmailNotifyIsEnabled() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getReference());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateCancelledEmailFor(transaction, mandate, payer);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_CANCELLED, transaction, EMAIL, emailPersonalisation);
    }

    @Test
    public void shouldSendPaymentConfirmedEmailIfEmailNotifyIsEnabled() {
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig);

        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("amount", "123.45");
        emailPersonalisation.put("payment reference", transaction.getPaymentRequest().getReference());
        emailPersonalisation.put("collection date", "21/05/2018");
        emailPersonalisation.put("bank account last 2 digits", "******" + payer.getAccountNumberLastTwoDigits());
        emailPersonalisation.put("SUN", "THE-CAKE-IS-A-LIE");
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");


        userNotificationService.sendPaymentConfirmedEmailFor(transaction, payer, LocalDate.parse("2018-05-21"));

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.PAYMENT_CONFIRMED, transaction, EMAIL, emailPersonalisation);
    }

}
