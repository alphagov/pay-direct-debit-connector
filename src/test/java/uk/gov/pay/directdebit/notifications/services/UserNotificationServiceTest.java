package uk.gov.pay.directdebit.notifications.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.common.services.SunService;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.clients.AdminUsersClient;
import uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;

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
    @Mock
    private SunService mockSunService;

    private static final String EMAIL = "ksdfhkjsdh@sdjkfh.test";
    private PayerFixture payerFixture = aPayerFixture().withEmail(EMAIL);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture().withPayerFixture(
            payerFixture);

    private Payment payment = aPaymentFixture()
            .withMandateFixture(mandateFixture)
            .withAmount(12345L)
            .toEntity();

    @Before
    public void setUp() {
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);
        when(mockLinksConfig.getDirectDebitGuaranteeUrl()).thenReturn("https://frontend.url.test/direct-debit-guarantee");
        userNotificationService = new UserNotificationService(mockAdminUsersClient, mockDirectDebitConfig, mockSunService);
    }

    @Test
    public void shouldSendMandateFailedEmail() {
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference().toString());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateFailedEmailFor(mandateFixture.toEntity());

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_FAILED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendMandateCreatedEmail() {
        Mandate mandate = MandateFixture.aMandateFixture().withPayerFixture(payerFixture).toEntity();
        SunName sunName = SunName.of("test sun Name");
        when(mockSunService.getSunNameFor(mandate)).thenReturn(Optional.of(sunName));
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandate.getMandateBankStatementReference().get().toString());
        emailPersonalisation.put("bank account last 2 digits", mandate.getPayer().get().getAccountNumberLastTwoDigits());
        emailPersonalisation.put("statement name", sunName.toString());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateCreatedEmailFor(mandate);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.ON_DEMAND_MANDATE_CREATED, mandate, emailPersonalisation);
    }

    public void shouldNotSendMandateCreatedEmail_whenSunNameUnavailable() {
        Mandate mandate = MandateFixture.aMandateFixture().withPayerFixture(payerFixture).toEntity();
        when(mockSunService.getSunNameFor(mandate)).thenReturn(Optional.empty());
        userNotificationService.sendMandateCreatedEmailFor(mandate);
        verifyZeroInteractions(mockAdminUsersClient);
    }

    @Test
    public void shouldSendMandateCancelledEmail() {
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("mandate reference", mandateFixture.getMandateReference().toString());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendMandateCancelledEmailFor(mandateFixture.toEntity());

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.MANDATE_CANCELLED, mandateFixture.toEntity(), emailPersonalisation);
    }

    @Test
    public void shouldSendPaymentConfirmedEmail() {
        Mandate mandate = mandateFixture.withPayerFixture(payerFixture).toEntity();

        Payment paymentWithMandate = fromPayment(payment).withMandate(mandate).build();

        SunName sunName = SunName.of("test sun Name");
        when(mockSunService.getSunNameFor(mandate)).thenReturn(Optional.of(sunName));
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("amount", "123.45");
        emailPersonalisation.put("mandate reference", mandate.getMandateBankStatementReference().get().toString());
        emailPersonalisation.put("collection date", payment.getChargeDate().get()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.UK)));
        emailPersonalisation.put("bank account last 2 digits", payerFixture.getAccountNumberLastTwoDigits());
        emailPersonalisation.put("statement name", sunName.toString());
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendPaymentConfirmedEmailFor(paymentWithMandate);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.ON_DEMAND_PAYMENT_CONFIRMED, mandate, emailPersonalisation);
    }

    @Test
    public void shouldSendPaymentFailedEmail() {
        HashMap<String, String> emailPersonalisation = new HashMap<>();
        emailPersonalisation.put("dd guarantee link", "https://frontend.url.test/direct-debit-guarantee");

        userNotificationService.sendPaymentFailedEmailFor(payment);

        verify(mockAdminUsersClient).sendEmail(EmailTemplate.PAYMENT_FAILED, mandateFixture.toEntity(), emailPersonalisation);
    }

}
