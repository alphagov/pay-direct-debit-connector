package uk.gov.pay.directdebit.mandate.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessPaymentDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessPaymentDao goCardlessPaymentDao;

    private TransactionFixture transactionFixture;
    @Before
    public void setup()  {
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        transactionFixture = TransactionFixture.aTransactionFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        goCardlessPaymentDao = testContext.getJdbi().onDemand(GoCardlessPaymentDao.class);
    }

    @Test
    public void shouldInsertAGoCardlessMandate() {
        String goCardlessPaymentId = "NA23434";
        Long id = goCardlessPaymentDao.insert(new GoCardlessPayment(transactionFixture.getId(), goCardlessPaymentId));
        Map<String, Object> goCardlessPayment = testContext.getDatabaseTestHelper().getGoCardlessPaymentById(id);
        assertThat(goCardlessPayment.get("id"), is(id));
        assertThat(goCardlessPayment.get("transaction_id"), is(transactionFixture.getId()));
        assertThat(goCardlessPayment.get("payment_id"), is(goCardlessPaymentId));
    }
}
