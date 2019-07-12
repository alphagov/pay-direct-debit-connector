package uk.gov.pay.directdebit.events.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.sql.Timestamp;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GovUkPayEventFixture.aGovUkPayEventFixture;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GovUkPayEventDaoITest {

    @DropwizardTestContext
    private TestContext testContext;

    private GovUkPayEventDao govUkPayEventDao;
    private Mandate mandate;

    @Before
    public void setUp() {
        govUkPayEventDao = testContext.getJdbi().onDemand(GovUkPayEventDao.class);

        var gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
        gatewayAccountFixture.insert(testContext.getJdbi());

        var mandateFixture = aMandateFixture()
                .withGatewayAccountFixture(gatewayAccountFixture)
                .insert(testContext.getJdbi());

        mandate = mandateFixture.toEntity();
    }

    @Test
    public void shouldInsertAnEvent() {

        var govUkPayEvent = aGovUkPayEventFixture()
                .withMandate(mandate)
                .toEntity();

        Long id = govUkPayEventDao.insert(govUkPayEvent);

        Map<String, Object> govUkPayEventMap = testContext.getDatabaseTestHelper().getGovUkPayEventById(id);

        assertThat(govUkPayEventMap.get("id"), is(id));
        assertThat(govUkPayEventMap.get("mandate_id"), is(mandate.getId()));
        assertThat(govUkPayEventMap.get("payment_id"), is(nullValue()));
        assertThat(govUkPayEventMap.get("event_date"), is(Timestamp.from(govUkPayEvent.getEventDate().toInstant())));
        assertThat(govUkPayEventMap.get("resource_type"), is(govUkPayEvent.getResourceType().toString()));
        assertThat(govUkPayEventMap.get("event_type"), is(govUkPayEvent.getEventType()));
    }
}
