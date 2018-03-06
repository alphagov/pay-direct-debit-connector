package uk.gov.pay.directdebit.mandate.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessMandateDaoIT {

    @DropwizardTestContext
    private TestContext testContext;

    private GoCardlessMandateDao mandateDao;
    private MandateFixture mandateFixture;

    private final String GOCARDLESS_MANDATE_ID = "NA23434";
    private GoCardlessMandateFixture testGoCardlessMandate;

    @Before
    public void setup()  {
        mandateDao = testContext.getJdbi().onDemand(GoCardlessMandateDao.class);
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().insert(testContext.getJdbi());
        PayerFixture payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId()).insert(testContext.getJdbi());
        mandateFixture = MandateFixture.aMandateFixture().withPayerId(payerFixture.getId()).insert(testContext.getJdbi());
        testGoCardlessMandate = aGoCardlessMandateFixture()
                .withMandateId(mandateFixture.getId())
                .withGoCardlessMandateId(GOCARDLESS_MANDATE_ID);
    }

    @Test
    public void shouldInsertAGoCardlessMandate() {
        Long id = mandateDao.insert(testGoCardlessMandate.toEntity());
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getGoCardlessMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("mandate_id"), is(mandateFixture.getId()));
        assertThat(mandate.get("gocardless_mandate_id"), is(GOCARDLESS_MANDATE_ID));
    }

    @Test
    public void shouldFindAGoCardlessMandateByEventResourceId() {
        testGoCardlessMandate.insert(testContext.getJdbi());
        GoCardlessMandate goCardlessMandate = mandateDao
                .findByEventResourceId(GOCARDLESS_MANDATE_ID).get();
        assertThat(goCardlessMandate.getId(), is(goCardlessMandate.getId()));
        assertThat(goCardlessMandate.getMandateId(), is(mandateFixture.getId()));
        assertThat(goCardlessMandate.getGoCardlessMandateId(), is(GOCARDLESS_MANDATE_ID));
    }

    @Test
    public void shouldNotFindAGoCardlessMandateByEventResourceId_ifResourceIdIsInvalid() {
        String resourceId = "non_existing_resourceId";
        assertThat(mandateDao.findByEventResourceId(resourceId), is(Optional.empty()));
    }
}
