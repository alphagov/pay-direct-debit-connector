package uk.gov.pay.directdebit.mandate.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessPaymentDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessPaymentDao goCardlessPaymentDao;

    private TransactionFixture transactionFixture;

    private final static String GOCARDLESS_PAYMENT_ID = "NA23434";

    private GoCardlessPaymentFixture goCardlessPaymentFixture;

    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup()  {
        gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture
                .aPaymentRequestFixture()
                .withGatewayAccountId(gatewayAccountFixture.getId())
                .insert(testContext.getJdbi());
        transactionFixture = TransactionFixture.aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        goCardlessPaymentDao = testContext.getJdbi().onDemand(GoCardlessPaymentDao.class);
        goCardlessPaymentFixture = aGoCardlessPaymentFixture()
                .withTransactionId(transactionFixture.getId())
                .withPaymentId(GOCARDLESS_PAYMENT_ID);
    }

    @Test
    public void shouldInsertAGoCardlessPayment() {
        Long id = goCardlessPaymentDao.insert(goCardlessPaymentFixture.toEntity());
        Map<String, Object> goCardlessPayment = testContext.getDatabaseTestHelper().getGoCardlessPaymentById(id);
        assertThat(goCardlessPayment.get("id"), is(id));
        assertThat(goCardlessPayment.get("transaction_id"), is(transactionFixture.getId()));
        assertThat(goCardlessPayment.get("payment_id"), is(GOCARDLESS_PAYMENT_ID));
    }

    @Test
    public void shouldFindAGoCardlessPaymentByEventResourceId() {
        goCardlessPaymentFixture.insert(testContext.getJdbi());
        GoCardlessPayment goCardlessPayment = goCardlessPaymentDao
                .findByEventResourceId(GOCARDLESS_PAYMENT_ID).get();
        assertThat(goCardlessPayment.getId(), is(goCardlessPaymentFixture.getId()));
        assertThat(goCardlessPayment.getTransactionId(), is(transactionFixture.getId()));
        assertThat(goCardlessPayment.getPaymentId(), is(GOCARDLESS_PAYMENT_ID));
    }

    @Test
    public void shouldNotFindAGoCardlessPaymentByEventResourceId_ifResourceIdIsInvalid() {
        String resourceId = "non_existing_resourceId";
        assertThat(goCardlessPaymentDao.findByEventResourceId(resourceId), is(Optional.empty()));
    }
}
