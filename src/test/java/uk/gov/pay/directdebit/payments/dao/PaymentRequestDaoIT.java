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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentRequestDao paymentRequestDao;
    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentRequestDao = testContext.getJdbi().onDemand(PaymentRequestDao.class);
        this.testPaymentRequest = aPaymentRequestFixture()
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert(testContext.getJdbi());
        aTokenFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAPaymentRequest() {
        String externalId = "externalId";
        Long id = paymentRequestDao.insert(testPaymentRequest.withExternalId(externalId).toEntity());
        Map<String, Object> foundPaymentRequest = testContext.getDatabaseTestHelper().getPaymentRequestById(id);
        assertThat(foundPaymentRequest.get("id"), is(id));
        assertThat(foundPaymentRequest.get("external_id"), is(externalId));
        assertThat(foundPaymentRequest.get("amount"), is(testPaymentRequest.getAmount()));
        assertThat(foundPaymentRequest.get("reference"), is(testPaymentRequest.getReference()));
        assertThat(foundPaymentRequest.get("description"), is(testPaymentRequest.getDescription()));
        assertThat(foundPaymentRequest.get("return_url"), is(testPaymentRequest.getReturnUrl()));
        assertThat((Timestamp) foundPaymentRequest.get("created_date"), isDate(testPaymentRequest.getCreatedDate()));
    }

    @Test
    public void shouldFindPaymentRequestById() {
        PaymentRequest paymentRequest = paymentRequestDao.findById(testPaymentRequest.getId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(testPaymentRequest.getAmount()));
        assertThat(paymentRequest.getCreatedDate(), is(testPaymentRequest.getCreatedDate()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(paymentRequest.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getReference(), is(testPaymentRequest.getReference()));
        assertThat(paymentRequest.getReturnUrl(), is(testPaymentRequest.getReturnUrl()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
    }

    @Test
    public void shouldNotFindPaymentRequestById_ifIdIsInvalid() {
        Long noExistingChargeId = 9876512L;
        assertThat(paymentRequestDao.findById(noExistingChargeId).isPresent(), is(false));
    }

    @Test
    public void shouldFindPaymentRequestByExternalId() {
        PaymentRequest paymentRequest = paymentRequestDao.findByExternalId(testPaymentRequest.getExternalId()).get();
        assertThat(paymentRequest.getId(), is(notNullValue()));
        assertThat(paymentRequest.getAmount(), is(testPaymentRequest.getAmount()));
        assertThat(paymentRequest.getCreatedDate(), is(testPaymentRequest.getCreatedDate()));
        assertThat(paymentRequest.getDescription(), is(testPaymentRequest.getDescription()));
        assertThat(paymentRequest.getGatewayAccountId(), is(testPaymentRequest.getGatewayAccountId()));
        assertThat(paymentRequest.getReference(), is(testPaymentRequest.getReference()));
        assertThat(paymentRequest.getReturnUrl(), is(testPaymentRequest.getReturnUrl()));
        assertThat(paymentRequest.getExternalId(), is(testPaymentRequest.getExternalId()));
    }

    @Test
    public void shouldNotFindPaymentRequestByExternalId_ifExternalIdIsInvalid() {
        String externalId = "non_existing_externalId";
        assertThat(paymentRequestDao.findByExternalId(externalId), is(Optional.empty()));
    }
}
