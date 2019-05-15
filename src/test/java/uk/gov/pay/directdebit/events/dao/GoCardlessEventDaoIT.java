package uk.gov.pay.directdebit.events.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture.aDirectDebitEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessEventDaoIT {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private GoCardlessEventDao goCardlessEventDao;

    @DropwizardTestContext
    private TestContext testContext;

    private final static Long EVENT_ID = 6L;
    private final static String GOCARDLESS_EVENT_ID = "dhg2342h3kjh";
    private final static String GOCARDLESS_ACTION = "something happened";
    private final static GoCardlessResourceType GOCARDLESS_RESOURCE_TYPE = PAYMENTS;
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode eventJson;
    private static final ZonedDateTime CREATED_AT = ZonedDateTime.parse("2017-12-30T12:30:40Z");

    private GoCardlessEvent goCardlessEvent;
    private MandateFixture testMandate;
    private GatewayAccountFixture gatewayAccountFixture;

    @Before
    public void setup() throws IOException {
        eventJson = objectMapper.readTree("{\"id\": \"somejson\"}");
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);
        gatewayAccountFixture = aGatewayAccountFixture().insert(testContext.getJdbi());
        testMandate = MandateFixture.aMandateFixture().withGatewayAccountFixture(gatewayAccountFixture).insert(testContext.getJdbi());
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withId(EVENT_ID)
                .insert(testContext.getJdbi());
        goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture()
                .withGoCardlessEventId(GOCARDLESS_EVENT_ID)
                .withAction(GOCARDLESS_ACTION)
                .withResourceType(GOCARDLESS_RESOURCE_TYPE)
                .withCreatedAt(CREATED_AT)
                .withJson(eventJson.toString())
                .toEntity();
    }

    @Test
    public void shouldInsertAnEvent() throws IOException {
        Long id = goCardlessEventDao.insert(goCardlessEvent);
        Map<String, Object> foundGoCardlessEvent = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);
        assertThat(foundGoCardlessEvent.get("id"), is(id));
        assertThat(foundGoCardlessEvent.get("event_id"), is(GOCARDLESS_EVENT_ID));
        assertThat(foundGoCardlessEvent.get("action"), is(GOCARDLESS_ACTION));
        assertThat(foundGoCardlessEvent.get("resource_type"), is(GOCARDLESS_RESOURCE_TYPE.toString()));
        assertThat(objectMapper.readTree(foundGoCardlessEvent.get("json").toString()), is(eventJson));
        assertThat((Timestamp) foundGoCardlessEvent.get("created_at"), isDate(CREATED_AT));
    }
    
    
    @Test
    public void shouldFindEventsForMandate() {
        GoCardlessEventFixture eventFixture = GoCardlessEventFixture.aGoCardlessEventFixture()
                .withResourceType(GoCardlessResourceType.MANDATES)
                .withMandateId(testMandate.getExternalId().toString())
                .withAction("NOT SURE")
                .insert(testContext.getJdbi());
        
        List<GoCardlessEvent> goCardlessEvents = goCardlessEventDao.findEventsForMandate(testMandate.getExternalId().toString());
        assertThat(goCardlessEvents.size(), is(1));
        GoCardlessEvent foundGoCardlessEvent = goCardlessEvents.get(0);
        assertThat(foundGoCardlessEvent.getResourceType(), is(eventFixture.getResourceType()));
        assertThat(foundGoCardlessEvent.getMandateId(), is(eventFixture.getMandateId()));
        assertThat(foundGoCardlessEvent.getAction(), is(eventFixture.getAction()));
    }

    // TODO I don't think we want to update the eventId in the new approach.
    /*@Test
    public void shouldUpdateEventIdAndReturnNumberOfAffectedRows() throws IOException {
        Long newEventId = 10L;
        aDirectDebitEventFixture()
                .withMandateId(testMandate.getId())
                .withId(newEventId)
                .insert(testContext.getJdbi());
        Long id = goCardlessEventDao.insert(goCardlessEvent);
//        int numOfUpdatedEvents = goCardlessEventDao.updateEventId(id, newEventId);
        Map<String, Object> eventAfterUpdate = testContext.getDatabaseTestHelper().getGoCardlessEventById(id);
//        assertThat(numOfUpdatedEvents, is(1));
        assertThat(eventAfterUpdate.get("internal_event_id"), is(newEventId));
        assertThat(eventAfterUpdate.get("event_id"), is(GOCARDLESS_EVENT_ID));
        assertThat(eventAfterUpdate.get("action"), is(GOCARDLESS_ACTION));
        assertThat(eventAfterUpdate.get("resource_type"), is(GOCARDLESS_RESOURCE_TYPE.toString()));
        assertThat(objectMapper.readTree(eventAfterUpdate.get("json").toString()), is(eventJson));
        assertThat((Timestamp) eventAfterUpdate.get("created_at"), isDate(CREATED_AT));
    }*/


}
