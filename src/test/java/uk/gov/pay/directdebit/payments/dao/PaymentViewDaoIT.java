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
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PayersFixture.aPayersFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class PaymentViewDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private PaymentViewDao paymentViewDao;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup() throws IOException, LiquibaseException {
        paymentViewDao = new PaymentViewDao(testContext.getJdbi());
        this.gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
    }

    @Test
    public void shouldReturnAllPaymentViews() throws Exception {
        for (int i = 0; i < 3; i++) {
            PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
                    .withGatewayAccountId(gatewayAccountFixture.getId())
                    .withReference("important reference " + i)
                    .withDescription("description " + i)
                    .insert(testContext.getJdbi());

            aPayersFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withAmount(1000l + i)
                    .insert(testContext.getJdbi());
        }

        List<PaymentView> viewList = paymentViewDao.searchPaymentView(gatewayAccountFixture.getExternalId(), 0l, 100l);
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

            aPayersFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withName("Joe Bog" + i)
                    .insert(testContext.getJdbi());
            aTransactionFixture()
                    .withPaymentRequestId(paymentRequest.getId())
                    .withGatewayAccountExternalId(gatewayAccountFixture.getExternalId())
                    .withAmount(1000l + i)
                    .insert(testContext.getJdbi());
        }

        List<PaymentView> viewList = paymentViewDao.searchPaymentView(gatewayAccountFixture.getExternalId(), 2l, 100l);
        assertThat(viewList.size(), is(1));
    }

    @Test
    public void shouldReturnAnEmptyList_whenNoMatchingGatewayAccounts() throws Exception {
        List<PaymentView> viewList = paymentViewDao.searchPaymentView("invalid-external-id", 0l, 100l);
        assertThat(viewList.isEmpty(), is(true));
    }
}
