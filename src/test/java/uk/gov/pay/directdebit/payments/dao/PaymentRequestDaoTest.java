package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.infra.DaoITestBase;
import uk.gov.pay.directdebit.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TokenFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.paymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TokenFixture.tokenFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;


public class PaymentRequestDaoTest extends DaoITestBase {

    @Rule
    public DropwizardAppWithPostgresRule postgres;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PaymentRequestDao paymentRequestDao;

    private PaymentRequestFixture testPaymentRequest;
    private TokenFixture testToken;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentRequestDao = jdbi.onDemand(PaymentRequestDao.class);
        this.testPaymentRequest = paymentRequestFixture(jdbi)
                .withGatewayAccountId(RandomUtils.nextLong(1, 99999))
                .insert();
       this.testToken = tokenFixture(jdbi)
                .withChargeId(testPaymentRequest.getId())
                .insert();
    }


    @Test
    public void shouldInsertAPaymentRequest() {
        Long id = paymentRequestDao.insert(testPaymentRequest.toEntity());
        Map<String, Object> foundPaymentRequest = databaseTestHelper.getPaymentRequestById(id);
        assertThat(foundPaymentRequest.get("id"), is(id));
        assertThat(foundPaymentRequest.get("external_id"), is(testPaymentRequest.getExternalId()));
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
        assertThat(paymentRequestDao.findByTokenId(externalId), is(Optional.empty()));
    }

    @Test
    public void shouldFindPaymentRequestByTokenId() {
        PaymentRequest paymentRequest = paymentRequestDao.findByTokenId(testToken.getToken()).get();
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
    public void shouldNotFindPaymentRequestByTokenId_ifTokenIdIsInvalid() {
        String tokenId = "non_existing_tokenId";
        assertThat(paymentRequestDao.findByTokenId(tokenId), is(Optional.empty()));
    }
}
