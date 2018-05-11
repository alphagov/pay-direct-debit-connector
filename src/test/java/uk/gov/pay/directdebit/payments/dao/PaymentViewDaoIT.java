package uk.gov.pay.directdebit.payments.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.params.CommaDelimitedSetParameter;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.params.SearchDateParams;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentViewDao paymentViewDao;
    private GatewayAccountFixture gatewayAccountFixture;
    private ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
    private ZonedDateTime zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7l);
    private SearchDateParams searchDateParams;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentViewDao = new PaymentViewDao(testContext.getJdbi());
        this.gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
        zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7L);
        searchDateParams = new SearchDateParams(zonedDateTime7DaysAgo, zonedDateTimeNow);
    }

    @Test
    public void shouldReturnAllPaymentViews() throws Exception {
        for (int i = 0; i < 3; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .insert(testContext.getJdbi());

            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(3));
    }

    @Test
    public void shouldReturnOnePaymentViewOnly() throws Exception {
        for (int i = 0; i < 3; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .insert(testContext.getJdbi());

            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(), 
                2L, 100L, null, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturnAnEmptyList_whenNoMatchingGatewayAccounts() throws Exception {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("invalid-external-id", 
                1L, 100L, null, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTwoPaymentView_withFromDateSet() {
        for (int i = 1; i < 4; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(i * 2))
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .insert(testContext.getJdbi());

            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                1L, 100L, zonedDateTime7DaysAgo.toString(), null, null, null, null, null, null,
                new SearchDateParams(zonedDateTime7DaysAgo, zonedDateTimeNow));
        List<PaymentView> paymentViewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(paymentViewList.size(), is(2));
        assertThat(paymentViewList.get(0).getCreatedDate().isAfter(zonedDateTime7DaysAgo), is(true));
        assertThat(paymentViewList.get(0).getCreatedDate().isBefore(zonedDateTimeNow), is(true));
        assertThat(paymentViewList.get(1).getCreatedDate().isAfter(zonedDateTime7DaysAgo), is(true));
        assertThat(paymentViewList.get(1).getCreatedDate().isBefore(zonedDateTimeNow), is(true));
    }

    @Test
    public void shouldReturn2PaymentView_withEmailSet() {
        List<String> emailList = Arrays.asList("jane@email.org", "joe.bog@email.org", "jane.bog@email.org", "joe@email.org");
        for (int i = 0; i < 4; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withEmail(emailList.get(i))
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, "bog", null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
        assertThat(viewList.get(0).getEmail().contains("bog"), is(true));
        assertThat(viewList.get(1).getEmail().contains("bog"), is(true));
    }

    @Test
    public void shouldReturn2PaymentView_withReferenceSet() {
        List<String> referenceList = Arrays.asList("MBKH45", "MBKI19", "MBKH46", "MBKI21");
        for (int i = 0; i < 4; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withId(i + 1)
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference(referenceList.get(i))
                    .withAmount(200 + i)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, "bkh", null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
        assertThat(viewList.get(0).getReference(), is(referenceList.get(2)));
        assertThat(viewList.get(1).getReference(), is(referenceList.get(0)));
    }

    @Test
    public void shouldReturn1PaymentView_withAmountSet() {
        for (int i = 0; i < 4; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withId(i + 1)
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference("ref" + i)
                    .withAmount(200 + i)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, null, 202L, null, 
                null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
        assertThat(viewList.get(0).getReference(), is("ref2"));
    }
    
    @Test
    public void shouldReturn2PaymentView_whenStateIsExternalStarted() {
        for (int i = 0; i < 4; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withId(i + 1)
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference("ref" + i)
                    .withAmount(200 + i)
                    .insert(testContext.getJdbi());
            aPayerFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withState(i % 2 == 0 ? PaymentState.AWAITING_DIRECT_DEBIT_DETAILS : PaymentState.SUCCESS)
                    .insert(testContext.getJdbi());
        }
        CommaDelimitedSetParameter param = new CommaDelimitedSetParameter("started");
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, null, 202L, param,
                null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
    }
}
