package uk.gov.pay.directdebit.payers.services;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.EncryptionConfig;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {
    private static final String SALT = "$2a$10$IhaXo6LIBhKIWOiGpbtPOu";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private PayerDao mockedPayerDao;

    @Mock
    private TransactionService mockedTransactionService;

    @Mock
    private EncryptionConfig mockedEncryptionConfig;

    private PayerService service;
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture();

    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
            .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withPaymentRequestId(paymentRequestFixture.getId());

    private PayerFixture testPayer = PayerFixture.aPayerFixture();
    private Map<String, String> createPaymentRequest = new HashMap<String, String>() {{
        put("account_holder_name", testPayer.getName());
        put("account_number", "12345678");
        put("sort_code", testPayer.getSortCode());
        put("email", testPayer.getEmail());
        put("address_line1", testPayer.getAddressLine1());
        put("address_line2", testPayer.getAddressLine2());
        put("city", testPayer.getAddressCity());
        put("postcode", testPayer.getAddressPostcode());
        put("country_code", testPayer.getAddressCountry());
        put("requires_authorisation", String.valueOf(testPayer.getAccountRequiresAuthorisation()));
    }};
    @Before
    public void setUp() throws Exception {
        when(mockedEncryptionConfig.getEncryptDBSalt()).thenReturn(SALT);
        service = new PayerService(mockedEncryptionConfig, mockedPayerDao, mockedTransactionService);
    }

    @Test
    public void shouldCreateAPayer() {
        when(mockedTransactionService.receiveDirectDebitDetailsFor(paymentRequestFixture.getExternalId()))
                .thenReturn(transactionFixture.toEntity());
        Payer payer = service.create(paymentRequestFixture.getExternalId(), createPaymentRequest);
        assertThat(payer.getId(), is(notNullValue()));
        assertThat(payer.getExternalId(), is(notNullValue()));
        assertThat(payer.getPaymentRequestId(), is(paymentRequestFixture.getId()));
        assertThat(payer.getName(), is(testPayer.getName()));
        assertThat(payer.getEmail(), is(testPayer.getEmail()));
        assertThat(payer.getAccountNumberLastTwoDigits(), is("78"));
        assertThat(payer.getAccountRequiresAuthorisation(), is(testPayer.getAccountRequiresAuthorisation()));
        assertThat(payer.getAccountNumber(), is(BCrypt.hashpw("12345678", SALT)));
        assertThat(payer.getSortCode(), is(BCrypt.hashpw(testPayer.getSortCode(), SALT)));
        assertThat(payer.getAddressLine1(), is(testPayer.getAddressLine1()));
        assertThat(payer.getAddressLine2(), is(testPayer.getAddressLine2()));
        assertThat(payer.getAddressPostcode(), is(testPayer.getAddressPostcode()));
        assertThat(payer.getAddressCity(), is(testPayer.getAddressCity()));
        assertThat(payer.getAddressCountry(), is(testPayer.getAddressCountry()));
        assertThat(payer.getCreatedDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
        verify(mockedTransactionService).payerCreatedFor(transactionFixture.toEntity());
    }
}
