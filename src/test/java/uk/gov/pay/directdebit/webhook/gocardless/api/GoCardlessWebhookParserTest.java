package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.*;

public class GoCardlessWebhookParserTest {

    private GoCardlessWebhookParser parser;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        parser = new GoCardlessWebhookParser(objectMapper);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String EVENT_ID = "AAA345";
    private final GoCardlessResourceType RESOURCE_TYPE = PAYMENTS;
    private final String ACTION = "action";
    private final ZonedDateTime CREATED_AT = ZonedDateTime.now();

    @Test
    public void shouldParseASingleEvent() throws IOException {
        String validEvent = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, "payment", CREATED_AT);
        String payload = buildEvents(validEvent);

        List<GoCardlessEvent> parsedEvents = parser.parse(payload);

        GoCardlessEvent firstEvent = parsedEvents.get(0);
        assertThat(firstEvent.getPaymentRequestEventId(), is(nullValue()));
        assertThat(firstEvent.getId(), is(nullValue()));
        assertThat(firstEvent.getAction(), is(ACTION));
        assertThat(firstEvent.getResourceType(), is(RESOURCE_TYPE));
        assertThat(firstEvent.getCreatedAt(), is(CREATED_AT));
        assertThat(firstEvent.getJson(), is(objectMapper.readTree(validEvent).toString()));
    }
    @Test
    public void shouldParseMultipleEvents() throws IOException {
        String secondEventId = "AAA345";
        GoCardlessResourceType secondEventResourceType = MANDATES;
        String secondEventAction = "action";

        String thirdEventId = "BBB345";
        GoCardlessResourceType thirdEventResourceType = PAYOUTS;
        String thirdEventAction = "action";

        ZonedDateTime eventCreatedAt = ZonedDateTime.now();

        String firstEventPayload = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, "payment", CREATED_AT);
        String secondEventPayload = buildValidEvent(secondEventId, secondEventAction, secondEventResourceType, "mandate", eventCreatedAt);
        String thirdEventPayload = buildValidEvent(thirdEventId, thirdEventAction, thirdEventResourceType, "payout", eventCreatedAt);
        String payload = buildEvents(firstEventPayload, secondEventPayload, thirdEventPayload);

        List<GoCardlessEvent> parsedEvents = parser.parse(payload);

        GoCardlessEvent firstEvent = parsedEvents.get(0);
        assertThat(firstEvent.getPaymentRequestEventId(), is(nullValue()));
        assertThat(firstEvent.getId(), is(nullValue()));
        assertThat(firstEvent.getAction(), is(ACTION));
        assertThat(firstEvent.getResourceType(), is(RESOURCE_TYPE));
        assertThat(firstEvent.getCreatedAt(), is(CREATED_AT));
        assertThat(firstEvent.getJson(), is(objectMapper.readTree(firstEventPayload).toString()));

        GoCardlessEvent secondEvent = parsedEvents.get(1);
        assertThat(secondEvent.getPaymentRequestEventId(), is(nullValue()));
        assertThat(secondEvent.getId(), is(nullValue()));
        assertThat(secondEvent.getAction(), is(secondEventAction));
        assertThat(secondEvent.getResourceType(), is(secondEventResourceType));
        assertThat(secondEvent.getCreatedAt(), is(eventCreatedAt));
        assertThat(secondEvent.getJson(), is(objectMapper.readTree(secondEventPayload).toString()));

        GoCardlessEvent thirdEvent = parsedEvents.get(2);
        assertThat(thirdEvent.getPaymentRequestEventId(), is(nullValue()));
        assertThat(thirdEvent.getId(), is(nullValue()));
        assertThat(thirdEvent.getAction(), is(thirdEventAction));
        assertThat(thirdEvent.getResourceType(), is(thirdEventResourceType));
        assertThat(thirdEvent.getCreatedAt(), is(eventCreatedAt));
        assertThat(thirdEvent.getJson(), is(objectMapper.readTree(thirdEventPayload).toString()));
    }

    @Test
    public void shouldReturnEmptyListIfNoEventsAreThere() throws IOException {
        String payload = buildEvents();
        List<GoCardlessEvent> parsedEvents = parser.parse(payload);
        assertThat(parsedEvents.isEmpty(), is(true));
    }

    @Test
    public void shouldThrow_ifWebhookPayloadIsNotValid() throws IOException {
        thrown.expect(WebhookParserException.class);
        thrown.expectMessage("Failed to parse webhooks, body: ");
        thrown.reportMissingExceptionWithMessage("WebhookParserException expected");
        parser.parse("");
    }
    private String buildValidEvent(String eventId, String action, GoCardlessResourceType resourceType, String linkKey, ZonedDateTime createdAt) {
        return String.format("{\n" +
                "      \"id\": \"%s\",\n" +
                "      \"created_at\": \"%s\",\n" +
                "      \"action\": \"%s\",\n" +
                "      \"resource_type\": \"%s\",\n" +
                "      \"links\": {\n" +
                "        \"%s\": \"PM123\"\n" +
                "      },\n" +
                "      \"details\": {\n" +
                "        \"origin\": \"gocardless\",\n" +
                "        \"cause\": \"payment_confirmed\",\n" +
                "        \"description\": \"Payment was confirmed as collected\"\n" +
                "      }\n" +
                "    }", eventId, createdAt.toString(), action, resourceType.toString().toLowerCase(), linkKey);
    }

    private String buildEvents(String... events) throws IOException {
        ArrayNode eventsArray = objectMapper.createArrayNode();
        for (String event: events) {
            eventsArray.add(objectMapper.readTree(event));
        }
        ObjectNode eventsNode = objectMapper.createObjectNode();
        eventsNode.set("events", eventsArray);
        return eventsNode.toString();
    }
}
