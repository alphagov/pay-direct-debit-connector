package uk.gov.pay.directdebit.mandate.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private MandateDao mandateDao;
    private Long payerId;
    private Long paymentRequestId;

    @Before
    public void setup() {
        GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture().insert(testContext.getJdbi());
        paymentRequestId = PaymentRequestFixture
                .aPaymentRequestFixture()
                .withGatewayAccountId(gatewayAccountFixture.getId())
                .insert(testContext.getJdbi()).getId();
        payerId = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestId).insert(testContext.getJdbi()).getId();
        mandateDao = testContext.getJdbi().onDemand(MandateDao.class);
    }

    @Test
    public void shouldInsertAMandate() {
        Long id = mandateDao.insert(new Mandate(payerId));
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("payer_id"), is(payerId));
        assertThat(mandate.get("external_id"), is(notNullValue()));
    }

    @Test
    public void shouldFindAMandateByTransactionId() {
        TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture().withPaymentRequestId(paymentRequestId).insert(testContext.getJdbi());
        MandateFixture mandateFixture = MandateFixture.aMandateFixture().withPayerId(payerId).insert(testContext.getJdbi());
        Mandate mandate = mandateDao.findByTransactionId(transactionFixture.getId()).get();
        assertThat(mandate.getId(), is(mandateFixture.getId()));
        assertThat(mandate.getPayerId(), is(payerId));
        assertThat(mandate.getExternalId(), is(notNullValue()));
    }

    @Test
    public void shouldNotFindAMandateByTransactionId_ifTransactionIdIsInvalid() {
        Long invalidTransactionId = 29L;
        assertThat(mandateDao.findByTransactionId(invalidTransactionId), is(Optional.empty()));
    }
}
