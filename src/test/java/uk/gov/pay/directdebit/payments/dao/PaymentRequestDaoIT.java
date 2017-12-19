package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.tokens.fixtures.TokenFixture.aTokenFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentRequestDaoIT {

    private static final ZonedDateTime CREATED_DATE = ZonedDateTime.parse("2017-12-30T12:30:40Z[UTC]");
    private static final String RETURN_URL = "https://return.url";
    private static final String REFERENCE = "reference";
    private static final String DESCRIPTION = "description";
    private static final long AMOUNT = 4L;
    private static final String EXTERNAL_ID = "externalId";
    private static final long GATEWAY_ACCOUNT_ID = 10L;

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentRequestDao paymentRequestDao;
    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentRequestDao = testContext.getJdbi().onDemand(PaymentRequestDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(GATEWAY_ACCOUNT_ID)
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
        testPaymentRequest.insert(testContext.getJdbi());
        PaymentRequest paymentRequest = paymentRequestDao.findById(testPaymentRequest.getId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(AMOUNT));
        assertThat(paymentRequest.getCreatedDate(), is(CREATED_DATE));
        assertThat(paymentRequest.getDescription(), is(DESCRIPTION));
        assertThat(paymentRequest.getGatewayAccountId(), is(GATEWAY_ACCOUNT_ID));
        assertThat(paymentRequest.getReference(), is(REFERENCE));
        assertThat(paymentRequest.getReturnUrl(), is(RETURN_URL));
        assertThat(paymentRequest.getExternalId(), is(EXTERNAL_ID));
    }

    @Test
    public void shouldNotFindPaymentRequestById_ifIdIsInvalid() {
        Long noExistingChargeId = 9876512L;
        assertThat(paymentRequestDao.findById(noExistingChargeId).isPresent(), is(false));
    }

    @Test
    public void shouldFindPaymentRequestByExternalId() {
        testPaymentRequest.insert(testContext.getJdbi());
        PaymentRequest paymentRequest = paymentRequestDao.findByExternalId(testPaymentRequest.getExternalId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(AMOUNT));
        assertThat(paymentRequest.getCreatedDate(), is(CREATED_DATE));
        assertThat(paymentRequest.getDescription(), is(DESCRIPTION));
        assertThat(paymentRequest.getGatewayAccountId(), is(GATEWAY_ACCOUNT_ID));
        assertThat(paymentRequest.getReference(), is(REFERENCE));
        assertThat(paymentRequest.getReturnUrl(), is(RETURN_URL));
        assertThat(paymentRequest.getExternalId(), is(EXTERNAL_ID));
    }

    @Test
    public void shouldNotFindPaymentRequestByExternalId_ifExternalIdIsInvalid() {
        String externalId = "non_existing_externalId";
        assertThat(paymentRequestDao.findByExternalId(externalId), is(Optional.empty()));
    }
}
