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
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentFixture.aPaymentFixture;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.aPaymentViewSearchParams;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewDaoIT {
    
    @DropwizardTestContext
    private TestContext testContext;

    private static final String GATEWAY_ACCOUNT_ID = "gateway-account-id";
    private static final String MANDATE_ID_1 = "mandate-1";
    private static final String MANDATE_ID_2 = "mandate-2";

    private PaymentViewDao paymentViewDao;

    private Payment mandate1Payment1;
    private Payment mandate1Payment2;
    private Payment mandate2Payment1;

    @Before
    public void setup() {
        paymentViewDao = new PaymentViewDao(testContext.getJdbi());

        GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
                .withExternalId(GATEWAY_ACCOUNT_ID)
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture1 = aMandateFixture()
                .withId(1L)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(MandateExternalId.valueOf(MANDATE_ID_1))
                .insert(testContext.getJdbi());

        MandateFixture mandateFixture2 = aMandateFixture()
                .withId(2L)
                .withGatewayAccountFixture(gatewayAccountFixture)
                .withExternalId(MandateExternalId.valueOf(MANDATE_ID_2))
                .insert(testContext.getJdbi());

        mandate1Payment1 = aPaymentFixture()
                .withId(1L)
                .withMandateFixture(mandateFixture1)
                .withReference("REF1234")
                .withAmount(100L)
                .withState(PaymentState.SUBMITTED_TO_PROVIDER)
                .withCreatedDate(ZonedDateTime.of(2019, 7, 7, 9, 30, 15, 0, UTC))
                .insert(testContext.getJdbi())
        .toEntity();

        mandate1Payment2 = aPaymentFixture()
                .withId(2L)
                .withMandateFixture(mandateFixture1)
                .withReference("ref5678")
                .withAmount(200L)
                .withState(PaymentState.FAILED)
                .withCreatedDate(ZonedDateTime.of(2019, 7, 7, 10, 30, 15, 0, UTC))
                .insert(testContext.getJdbi())
        .toEntity();

        mandate2Payment1 = aPaymentFixture()
                .withId(3L)
                .withMandateFixture(mandateFixture2)
                .withReference("foo")
                .withAmount(300L)
                .withState(PaymentState.PAID_OUT)
                .withCreatedDate(ZonedDateTime.of(2019, 7, 8, 10, 30, 15, 0, UTC))
                .insert(testContext.getJdbi())
        .toEntity();
    }

    @Test
    public void responseShouldContainExpectedData() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withReference("foo")
                .build();
        
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(viewList.size(), is(1));

        PaymentResponse paymentResponse = viewList.get(0);
        assertThat(paymentResponse.getCreatedDate(), is(mandate2Payment1.getCreatedDate()));
        assertThat(paymentResponse.getReference(), is(mandate2Payment1.getReference()));
        assertThat(paymentResponse.getAmount(), is(mandate2Payment1.getAmount()));
        assertThat(paymentResponse.getDescription(), is(mandate2Payment1.getDescription()));
        assertThat(paymentResponse.getPaymentExternalId(), is(mandate2Payment1.getExternalId()));
        assertThat(paymentResponse.getPaymentProvider(), is(mandate2Payment1.getMandate().getGatewayAccount().getPaymentProvider()));
        assertThat(paymentResponse.getMandateId(), is(mandate2Payment1.getMandate().getExternalId()));
        assertThat(paymentResponse.getProviderId(), is(mandate2Payment1.getProviderId().get()));
    }

    @Test
    public void searchWithoutParameters_shouldReturnAllResultsInSinglePage() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withPage(1)
                .withDisplaySize(100)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(3));
        assertThat(payments.get(0).getReference(), is(mandate2Payment1.getReference()));
        assertThat(payments.get(1).getReference(), is(mandate1Payment2.getReference()));
        assertThat(payments.get(2).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void shouldReturnAnEmptyList_whenNoMatchingGatewayAccounts() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withReference("not-found")
                .build();
        List<PaymentResponse> viewList = paymentViewDao.searchPaymentView(searchParams, "an-unknown-account-id");
        assertThat(viewList, hasSize(0));
    }

    @Test
    public void searchByToDate() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withToDateString("2019-07-08T00:00:00.000Z")
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(2));
        assertThat(payments.get(0).getReference(), is(mandate1Payment2.getReference()));
        assertThat(payments.get(1).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void searchByFromDate() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withFromDateString("2019-07-08T00:00:00.000Z")
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate2Payment1.getReference()));
    }

    @Test
    public void searchByDateRange() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withFromDateString("2019-07-07T10:00:00.000Z")
                .withToDateString("2019-07-08T00:00:00.000Z")
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate1Payment2.getReference()));
    }

    @Test
    public void searchByPartialReference() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withReference("ref")
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(2));
        assertThat(payments.get(0).getReference(), is(mandate1Payment2.getReference()));
        assertThat(payments.get(1).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void searchByAmount() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withAmount(100L)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void searchByMandateId() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withMandateId(MANDATE_ID_1)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(2));
        assertThat(payments.get(0).getReference(), is(mandate1Payment2.getReference()));
        assertThat(payments.get(1).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void searchByState() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withState("paidout")
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate2Payment1.getReference()));
    }

    @Test
    public void searchWithDisplaySize() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withDisplaySize(2)
                .withPage(1)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(2));
        assertThat(payments.get(0).getReference(), is(mandate2Payment1.getReference()));
        assertThat(payments.get(1).getReference(), is(mandate1Payment2.getReference()));
    }

    @Test
    public void searchByPage() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withDisplaySize(2)
                .withPage(2)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate1Payment1.getReference()));
    }

    @Test
    public void searchByAllParams() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams()
                .withReference("ref")
                .withAmount(200L)
                .withMandateId(MANDATE_ID_1)
                .withFromDateString("2019-07-07T10:00:00.000Z")
                .withToDateString("2019-07-08T00:00:00.000Z")
                .withState("failed")
                .withDisplaySize(2)
                .withPage(1)
                .build();

        List<PaymentResponse> payments = paymentViewDao.searchPaymentView(searchParams, GATEWAY_ACCOUNT_ID);
        assertThat(payments.size(), is(1));
        assertThat(payments.get(0).getReference(), is(mandate1Payment2.getReference()));
    }
}
