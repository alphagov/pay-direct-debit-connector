package uk.gov.pay.directdebit.payments.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.params.SearchDateParams;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payers.fixtures.PayerFixture.aPayerFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentViewDao paymentViewDao;
    private ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
    private ZonedDateTime zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7L);
    private SearchDateParams searchDateParams;

    @Before
    public void setup() {
        paymentViewDao = new PaymentViewDao(testContext.getJdbi());
        zonedDateTimeNow = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1L);
        zonedDateTime7DaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7L);
        searchDateParams = new SearchDateParams(zonedDateTime7DaysAgo, zonedDateTimeNow);
    }

    @Test
    public void shouldReturnAllPaymentViews() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withPage(0L)
                .withDisplaySize(100L)
                .withSearchDateParams(searchDateParams);
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(3));
    }

    @Test
    public void shouldReturnOnePaymentViewOnly() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withPage(2L)
                .withDisplaySize(1L)
                .withSearchDateParams(searchDateParams);
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturnAnEmptyList_whenNoMatchingGatewayAccounts() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("invalid-external-id");
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnTwoPaymentView_withFromDateSet() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 1; i < 4; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(i * 2))
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withPage(1L)
                .withDisplaySize(100L)
                .withFromDateString(zonedDateTime7DaysAgo.toString())
                .withSearchDateParams(new SearchDateParams(zonedDateTime7DaysAgo, zonedDateTimeNow));
        List<PaymentResponse> paymentViewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(paymentViewList.size(), is(2));
        assertThat(paymentViewList.get(0).getCreatedDate().isAfter(zonedDateTime7DaysAgo), is(true));
        assertThat(paymentViewList.get(0).getCreatedDate().isBefore(zonedDateTimeNow), is(true));
        assertThat(paymentViewList.get(1).getCreatedDate().isAfter(zonedDateTime7DaysAgo), is(true));
        assertThat(paymentViewList.get(1).getCreatedDate().isBefore(zonedDateTimeNow), is(true));
    }
    
    @Test
    public void shouldReturn2PaymentView_withReferenceSet() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        List<String> referenceList = Arrays.asList("MBKH45", "MBKI19", "MBKH46", "MBKI21");
        for (int i = 0; i < 4; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference(referenceList.get(i))
                    .withAmount(((long) 200 + i))
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withReference("bkh");
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(2));
        assertThat(viewList.get(0).getReference(), is(referenceList.get(2)));
        assertThat(viewList.get(1).getReference(), is(referenceList.get(0)));
    }

    @Test
    public void shouldReturn1PaymentView_withAmountSet() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 4; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("ref" + i)
                    .withAmount(((long) 200 + i))
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withAmount(202L)
                .withSearchDateParams(searchDateParams);
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
        assertThat(viewList.get(0).getReference(), is("ref2"));
    }

    @Test
    public void shouldReturnOnePaymentViewWhenPaymentCreated() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());

        for (int i = 0; i < 3; i++) {
            MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
            aPayerFixture()
                    .withMandateId(mandateFixture.getId())
                    .insert(testContext.getJdbi());
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withPage(2L)
                .withDisplaySize(100L)
                .withSearchDateParams(searchDateParams);
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturnCount_whenRecordsExist() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        MandateFixture mandateFixture = aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .insert(testContext.getJdbi());
        for (int i = 0; i < 3; i++) {
            aPaymentFixture()
                    .withId((long) i)
                    .withMandateFixture(mandateFixture)
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .withAmount(1000L + i)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withMandateId(mandateFixture.getExternalId().toString());
        Long count = paymentViewDao.getPaymentViewCount(searchParams);
        assertThat(count, is(3L));
    }

    @Test
    public void shouldReturn3Records_withMatchingEmailsOnly() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateExternalId anotherMandateExternalId = MandateExternalId.valueOf("another-external-id");
        MandateFixture mandateFixture1 = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        MandateFixture mandateFixture2 = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(anotherMandateExternalId)
                .insert(testContext.getJdbi());
        PayerFixture payerFixture1 = aPayerFixture()
                .withMandateId(mandateFixture1.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture2.getId())
                .withEmail("j.doe@mail.test")
                .insert(testContext.getJdbi());
        for (int i = 0; i < 6; i++) {
            if (i % 2 == 0) {
                aPaymentFixture()
                        .withMandateFixture(mandateFixture2)
                        .insert(testContext.getJdbi());
                continue;
            }
            aPaymentFixture()
                    .withMandateFixture(mandateFixture1)
                    .insert(testContext.getJdbi());
        }
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withMandateId(mandateFixture1.getExternalId().toString());
        Long count = paymentViewDao.getPaymentViewCount(searchParams);
        assertThat(count, is(3L));
        List<PaymentResponse> paymentViews = paymentViewDao.searchPaymentView(searchParams);
        assertThat(paymentViews.size(), is(3));
    }

    @Test
    public void shouldReturn1Records_whenSearchingByNewExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.FAILED)
                .insert(testContext.getJdbi());

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withState("started");

        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturn1Records_whenSearchingByPendingExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.PENDING)
                .insert(testContext.getJdbi());

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withState("pending");

        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturn1Records_whenSearchingByUserCancelNotEligibleExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.USER_CANCEL_NOT_ELIGIBLE)
                .insert(testContext.getJdbi());

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withState("cancelled");

        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturn1Records_whenSearchingBySuccessExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.SUCCESS)
                .insert(testContext.getJdbi());

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withState("success");

        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturn6Records_whenSearchingByFailedExternalState() {
        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId("gateway-external-id")
                .insert(testContext.getJdbi());
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("a-mandate-external-id");
        MandateFixture mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(mandateExternalId)
                .insert(testContext.getJdbi());
        aPayerFixture()
                .withMandateId(mandateFixture.getId())
                .withEmail("j.citizen@mail.test")
                .insert(testContext.getJdbi());

        aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withState(PaymentState.NEW)
                .insert(testContext.getJdbi());
        for (int i = 0; i < 6; i++) {
            if (i % 3 == 0) {
                aPaymentFixture()
                        .withMandateFixture(mandateFixture)
                        .withState(PaymentState.CANCELLED)
                        .insert(testContext.getJdbi());
                continue;
            }
            if (i % 2 == 0) {
                aPaymentFixture()
                        .withMandateFixture(mandateFixture)
                        .withState(PaymentState.EXPIRED)
                        .insert(testContext.getJdbi());
                continue;
            }
            aPaymentFixture()
                    .withMandateFixture(mandateFixture)
                    .withState(PaymentState.FAILED)
                    .insert(testContext.getJdbi());
        }

        PaymentViewSearchParams searchParams = new PaymentViewSearchParams(gatewayAccountFixture.getExternalId())
                .withState("failed");

        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams);
        assertThat(viewList.size(), is(6));
    }
}
