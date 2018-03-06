package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.regexp.internal.RE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.webhook.gocardless.exception.InvalidWebhookException;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WebhookParserTest {

    private WebhookParser parser;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        parser = new WebhookParser(objectMapper);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String EVENT_ID = "AAA345";
    private final String RESOURCE_TYPE = "resource";
    private final String ACTION = "action";
    private final ZonedDateTime CREATED_AT = ZonedDateTime.now();

    @Test
    public void shouldParseASingleEvent() throws IOException {
        String validEvent = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, CREATED_AT);
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
        String eventId = "AAA345";
        String resourceType = "resource";
        String action = "action";
        ZonedDateTime createdAt = ZonedDateTime.now();
        String firstEventPayload = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, CREATED_AT);
        String secondEventPayload = buildValidEvent(eventId, action, resourceType, createdAt);
        String payload = buildEvents(firstEventPayload, secondEventPayload);

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
        assertThat(secondEvent.getAction(), is(action));
        assertThat(secondEvent.getResourceType(), is(resourceType));
        assertThat(secondEvent.getCreatedAt(), is(createdAt));
        assertThat(secondEvent.getJson(), is(objectMapper.readTree(secondEventPayload).toString()));
    }

    @Test
    public void shouldReturnEmptyListIfNoEventsAreThere() throws IOException {
        String payload = buildEvents();
        List<GoCardlessEvent> parsedEvents = parser.parse(payload);
        assertThat(parsedEvents.isEmpty(), is(true));
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

    @Test
    public void shouldThrow_ifWebhookPayloadIsNotValid() throws IOException {
        thrown.expect(WebhookParserException.class);
        thrown.expectMessage("Failed to parse webhooks, body: ");
        thrown.reportMissingExceptionWithMessage("WebhookParserException expected");
        parser.parse("");
    }
    private String buildValidEvent(String eventId, String action, String resourceType, ZonedDateTime createdAt) {
        return String.format("{\n" +
                "      \"id\": \"%s\",\n" +
                "      \"created_at\": \"%s\",\n" +
                "      \"action\": \"%s\",\n" +
                "      \"resource_type\": \"%s\",\n" +
                "      \"links\": {\n" +
                "        \"payment\": \"PM123\"\n" +
                "      },\n" +
                "      \"details\": {\n" +
                "        \"origin\": \"gocardless\",\n" +
                "        \"cause\": \"payment_confirmed\",\n" +
                "        \"description\": \"Payment was confirmed as collected\"\n" +
                "      }\n" +
                "    }", eventId, createdAt.toString(), action, resourceType);
    }
}
