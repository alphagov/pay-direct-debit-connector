package uk.gov.pay.directdebit.events.dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import uk.gov.pay.directdebit.DirectDebitConnectorApp;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.junit.DropwizardConfig;
import uk.gov.pay.directdebit.junit.DropwizardJUnitRunner;
import uk.gov.pay.directdebit.junit.DropwizardTestContext;
import uk.gov.pay.directdebit.junit.TestContext;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class SandboxEventDaoIT {

    private static final ZonedDateTime CREATED_AT = ZonedDateTime.parse("2017-12-30T12:30:40Z");
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private SandboxEventDao sandboxEventDao;
    @DropwizardTestContext
    private TestContext testContext;

    @Before
    public void setup() {
        sandboxEventDao = testContext.getJdbi().onDemand(SandboxEventDao.class);
    }

    @Test
    public void shouldInsertAnEvent() throws IOException {
        SandboxEvent sandboxEvent = SandboxEvent.SandboxEventBuilder.aSandboxEvent()
                .withMandateId(SandboxMandateId.valueOf("aMandateId"))
                .withPaymentId(SandboxPaymentId.valueOf("aPaymentId"))
                .withEventAction("anEventAction")
                .withEventCause("anEventCause")
                .withCreatedAt(CREATED_AT)
                .build();

        Long id = sandboxEventDao.insert(sandboxEvent);

        Map<String, Object> foundSandboxEvent = testContext.getDatabaseTestHelper().getSandboxEventById(id);
        assertThat(foundSandboxEvent.get("id"), is(id));
        assertThat(foundSandboxEvent.get("mandate_id"), is("aMandateId"));
        assertThat(foundSandboxEvent.get("payment_id"), is("aPaymentId"));
        assertThat(foundSandboxEvent.get("event_action"), is("anEventAction"));
        assertThat(foundSandboxEvent.get("event_cause"), is("anEventCause"));
        assertThat((Timestamp) foundSandboxEvent.get("created_at"), isDate(CREATED_AT));
    }
}
