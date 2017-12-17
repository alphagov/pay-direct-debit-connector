package uk.gov.pay.directdebit.mandate.dao;

import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class MandateDaoIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @DropwizardTestContext
    private TestContext testContext;

    private MandateDao mandateDao;

    @Before
    public void setup() throws IOException, LiquibaseException {
        mandateDao = testContext.getJdbi().onDemand(MandateDao.class);
    }

    @Test
    public void shouldInsertAMandate() {

        long payerId = 2L;

        Long id = mandateDao.insert(new Mandate(payerId));
        Map<String, Object> mandate = testContext.getDatabaseTestHelper().getMandateById(id);
        assertThat(mandate.get("id"), is(id));
        assertThat(mandate.get("payer_id"), is(payerId));
        assertThat(mandate.get("external_id"), is(notNullValue()));
    }
}
