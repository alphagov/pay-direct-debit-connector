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
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessMandateDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessMandateDao mandateDao;
    private MandateFixture mandateFixture;
    @Before
    public void setup()  {
        mandateDao = testContext.getJdbi().onDemand(GoCardlessMandateDao.class);
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        mandateFixture = MandateFixture.aMandateFixture().withPayerId(payerFixture.getId()).insert(testContext.getJdbi());
    }

    @Test
    public void shouldInsertAGoCardlessMandate() {
        String goCardlessMandateId = "NA23434";
        Long id = mandateDao.insert(new GoCardlessMandate(mandateFixture.getId(), goCardlessMandateId));
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getGoCardlessMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("mandate_id"), is(mandateFixture.getId()));
        assertThat(mandate.get("gocardless_mandate_id"), is(goCardlessMandateId));
    }
}
