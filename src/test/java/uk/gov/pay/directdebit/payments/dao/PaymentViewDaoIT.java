package uk.gov.pay.directdebit.payments.dao;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentView;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.params.SearchDateParams;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentViewDao paymentViewDao;
    private ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
    private ZonedDateTime zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7l);
    private SearchDateParams searchDateParams;

    @Before
    public void setup()  {
        paymentViewDao = new PaymentViewDao(testContext.getJdbi());
        zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
        zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7L);
        searchDateParams = new SearchDateParams(zonedDateTime7DaysAgo, zonedDateTimeNow);
    }

    @Test
    public void shouldReturnAllPaymentViews()  {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(3));
    }

    @Test
    public void shouldReturnOnePaymentViewOnly() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                2L, 100L, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturnAnEmptyList_whenNoMatchingGatewayAccounts() throws Exception {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("invalid-external-id",
                1L, 100L, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTwoPaymentView_withFromDateSet() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 1; i < 4; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(i * 2))
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                1L, 100L, zonedDateTime7DaysAgo.toString(), null, null, null, null, null,
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
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        List<String> emailList = Arrays.asList("jane@example.com", "joe.bog@example.com", "jane.bog@example.com", "joe@example.com");
        for (int i = 0; i < 4; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withEmail(emailList.get(i))
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, "bog", null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
        assertThat(viewList.get(0).getEmail().contains("bog"), is(true));
        assertThat(viewList.get(1).getEmail().contains("bog"), is(true));
    }

    @Test
    public void shouldReturn2PaymentView_withReferenceSet() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        List<String> referenceList = Arrays.asList("MBKH45", "MBKI19", "MBKH46", "MBKI21");
        for (int i = 0; i < 4; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference(referenceList.get(i))
                    .withAmount(((long) 200 + i))
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, "bkh", null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
        assertThat(viewList.get(0).getReference(), is(referenceList.get(2)));
        assertThat(viewList.get(1).getReference(), is(referenceList.get(0)));
    }

    @Test
    public void shouldReturn2PaymentView_withAmountSet() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 4; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("ref" + i)
                    .withAmount(((long) 200 + i))
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                0L, 100L, null, null, null, null, 202L, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
        assertThat(viewList.get(0).getReference(), is("ref2"));
    }

    @Test
    public void shouldReturnOnePaymentViewWhenPaymentCreated() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aTransactionFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId(),
                2L, 100L, null, null, null, null, null, null, searchDateParams);
        List<PaymentView> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }
}
