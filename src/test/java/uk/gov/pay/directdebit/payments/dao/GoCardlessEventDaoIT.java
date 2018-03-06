package uk.gov.pay.directdebit.payments.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture.aPaymentRequestEventFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.util.ZonedDateTimeTimestampMatcher.isDate;

@RunWith(DropwizardJUnitRunner.class)
@DropwizardConfig(app = DirectDebitConnectorApp.class, config = "config/test-it-config.yaml")
public class GoCardlessEventDaoIT {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private GoCardlessEventDao goCardlessEventDao;

    @DropwizardTestContext
    private TestContext testContext;

    private final static Long PAYMENT_REQUEST_EVENTS_ID = 6L;
    private final static String GOCARDLESS_EVENT_ID = "dhg2342h3kjh";
    private final static String GOCARDLESS_ACTION = "something happened";
    private final static String GOCARDLESS_RESOURCE_TYPE = "payment";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode eventJson;
    private static final ZonedDateTime CREATED_AT = ZonedDateTime.parse("2017-12-30T12:30:40Z[UTC]");

    private GoCardlessEvent goCardlessEvent;
    private PaymentRequestFixture testPaymentRequest;

    @Before
    public void setup() throws IOException, LiquibaseException {
        eventJson = objectMapper.readTree("{\"id\": \"somejson\"}");
        goCardlessEventDao = testContext.getJdbi().onDemand(GoCardlessEventDao.class);
        testPaymentRequest = aPaymentRequestFixture().insert(testContext.getJdbi());
        aPaymentRequestEventFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withId(PAYMENT_REQUEST_EVENTS_ID)
                .insert(testContext.getJdbi());
        goCardlessEvent = GoCardlessEventFixture.aGoCardlessEventFixture()
                .withPaymentRequestEventsId(PAYMENT_REQUEST_EVENTS_ID)
                .withEventId(GOCARDLESS_EVENT_ID)
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
        assertThat(foundGoCardlessEvent.get("payment_request_events_id"), is(PAYMENT_REQUEST_EVENTS_ID));
        assertThat(foundGoCardlessEvent.get("event_id"), is(GOCARDLESS_EVENT_ID));
        assertThat(foundGoCardlessEvent.get("action"), is(GOCARDLESS_ACTION));
        assertThat(foundGoCardlessEvent.get("resource_type"), is(GOCARDLESS_RESOURCE_TYPE));
        assertThat(objectMapper.readTree(foundGoCardlessEvent.get("json").toString()), is(eventJson));
        assertThat((Timestamp) foundGoCardlessEvent.get("created_at"), isDate(CREATED_AT));
    }

    @Test
    public void shouldFindAGoCardlessEventById() throws IOException {
        Long id = goCardlessEventDao.insert(goCardlessEvent);
        GoCardlessEvent foundEvent = goCardlessEventDao.findById(id).get();
        assertThat(foundEvent.getPaymentRequestEventId(), is(PAYMENT_REQUEST_EVENTS_ID));
        assertThat(foundEvent.getEventId(), is(GOCARDLESS_EVENT_ID));
        assertThat(foundEvent.getAction(), is(GOCARDLESS_ACTION));
        assertThat(foundEvent.getResourceType(), is(GOCARDLESS_RESOURCE_TYPE));
        assertThat(foundEvent.getResourceId(), is(nullValue()));
        assertThat(objectMapper.readTree(foundEvent.getJson()), is(eventJson));
        assertThat(foundEvent.getCreatedAt(), is(CREATED_AT));
    }

    @Test
    public void shouldNotFindAGoCardlessEventById_IdIsInvalid() {
        assertThat(goCardlessEventDao.findById(38L), is(Optional.empty()));
    }

    @Test
    public void shouldUpdatePaymentRequestEventIdAndReturnNumberOfAffectedRows() throws IOException {
        Long newEventId = 10L;
        aPaymentRequestEventFixture()
                .withPaymentRequestId(testPaymentRequest.getId())
                .withId(newEventId)
                .insert(testContext.getJdbi());
        Long id = goCardlessEventDao.insert(goCardlessEvent);
        int numOfUpdatedEvents = goCardlessEventDao.updatePaymentRequestEventId(id, newEventId);
        GoCardlessEvent eventAfterUpdate = goCardlessEventDao.findById(id).get();
        assertThat(numOfUpdatedEvents, is(1));
        assertThat(eventAfterUpdate.getPaymentRequestEventId(), is(newEventId));
        assertThat(eventAfterUpdate.getEventId(), is(GOCARDLESS_EVENT_ID));
        assertThat(eventAfterUpdate.getAction(), is(GOCARDLESS_ACTION));
        assertThat(eventAfterUpdate.getResourceType(), is(GOCARDLESS_RESOURCE_TYPE));
        assertThat(eventAfterUpdate.getResourceId(), is(nullValue()));
        assertThat(objectMapper.readTree(eventAfterUpdate.getJson()), is(eventJson));
        assertThat(eventAfterUpdate.getCreatedAt(), is(CREATED_AT));
    }


}
