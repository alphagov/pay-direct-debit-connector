package uk.gov.pay.directdebit.payments.dao;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestDaoIT {

    private static final ZonedDateTime CREATED_DATE = ZonedDateTime.parse("2017-12-30T12:30:40Z");
    private static final String RETURN_URL = "https://return.url";
    private static final String REFERENCE = "reference";
    private static final String DESCRIPTION = "description";
    private static final long AMOUNT = 4L;
    private static final String EXTERNAL_ID = "externalId";

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentRequestDao paymentRequestDao;
    private GatewayAccountFixture testGatewayAccount;
    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() {
        paymentRequestDao = testContext.getJdbi().onDemand(PaymentRequestDao.class);
        this.testGatewayAccount = aGatewayAccountFixture().insert(testContext.getJdbi());
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .withExternalId(EXTERNAL_ID)
                .withAmount(AMOUNT)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withCreatedDate(CREATED_DATE);
    }

    @Test
    public void shouldInsertAPaymentRequest() {
        Long id = paymentRequestDao.insert(testPaymentRequest.toEntity());
        Map<String, Object> foundPaymentRequest = testContext.getDatabaseTestHelper().getPaymentRequestById(id);
        assertThat(foundPaymentRequest.get("id"), is(id));
        assertThat(foundPaymentRequest.get("external_id"), is(EXTERNAL_ID));
        assertThat(foundPaymentRequest.get("amount"), is(AMOUNT));
        assertThat(foundPaymentRequest.get("reference"), is(REFERENCE));
        assertThat(foundPaymentRequest.get("description"), is(DESCRIPTION));
        assertThat(foundPaymentRequest.get("return_url"), is(RETURN_URL));
        assertThat((Timestamp) foundPaymentRequest.get("created_date"), isDate(CREATED_DATE));
    }

    @Test
    public void shouldFindPaymentRequestById() {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        PaymentRequestFixture paymentRequestFixture = aPaymentRequestFixture()
                .withGatewayAccountId(testGatewayAccount.getId())
                .withExternalId(EXTERNAL_ID)
                .withAmount(AMOUNT)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withCreatedDate(CREATED_DATE)
                .withPayerFixture(payerFixture)
                .insert(testContext.getJdbi());
        PaymentRequest paymentRequest = paymentRequestDao.findById(paymentRequestFixture.getId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(AMOUNT));
        assertThat(paymentRequest.getCreatedDate(), is(CREATED_DATE));
        assertThat(paymentRequest.getDescription(), is(DESCRIPTION));
        assertThat(paymentRequest.getGatewayAccountId(), is(testGatewayAccount.getId()));
        assertThat(paymentRequest.getReference(), is(REFERENCE));
        assertThat(paymentRequest.getReturnUrl(), is(RETURN_URL));
        assertThat(paymentRequest.getExternalId(), is(EXTERNAL_ID));
        assertThat(paymentRequest.getPayer(), is(payerFixture.toEntity()));
    }

    @Test
    public void shouldNotFindPaymentRequestById_ifIdIsInvalid() {
        Long noExistingChargeId = 9876512L;
        assertThat(paymentRequestDao.findById(noExistingChargeId).isPresent(), is(false));
    }

    @Test
    public void shouldFindPaymentRequestByExternalIdAndGatewayAccountExternalId() {
        testPaymentRequest.insert(testContext.getJdbi());
        PaymentRequest paymentRequest = paymentRequestDao.findByExternalIdAndAccountExternalId(testPaymentRequest.getExternalId(), testGatewayAccount.getExternalId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(AMOUNT));
        assertThat(paymentRequest.getCreatedDate(), is(CREATED_DATE));
        assertThat(paymentRequest.getDescription(), is(DESCRIPTION));
        assertThat(paymentRequest.getGatewayAccountId(), is(testGatewayAccount.getId()));
        assertThat(paymentRequest.getReference(), is(REFERENCE));
        assertThat(paymentRequest.getReturnUrl(), is(RETURN_URL));
        assertThat(paymentRequest.getExternalId(), is(EXTERNAL_ID));
    }

    @Test
    public void shouldNotFindPaymentRequestByExternalIdAndGatewayAccountExternalId_ifExternalIdIsInvalid() {
        String externalId = "non_existing_externalId";
        assertThat(paymentRequestDao.findByExternalIdAndAccountExternalId(externalId, testGatewayAccount.getExternalId()), is(Optional.empty()));
    }

    @Test
    public void shouldNotFindPaymentRequestByExternalIdAndGatewayAccountExternalId_ifGatewayAccountIsInvalid() {
        testPaymentRequest.insert(testContext.getJdbi());
        String gatewayExternalId = "non_existing_externalId";
        assertThat(paymentRequestDao.findByExternalIdAndAccountExternalId(testPaymentRequest.getExternalId(), gatewayExternalId), is(Optional.empty()));
    }
}
