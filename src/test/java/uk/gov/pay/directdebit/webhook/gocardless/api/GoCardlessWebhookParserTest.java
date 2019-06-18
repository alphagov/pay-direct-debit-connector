package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYMENTS;
import static uk.gov.pay.directdebit.payments.model.GoCardlessResourceType.PAYOUTS;

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
        GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation_identifier");
        String validEvent = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, "payment", CREATED_AT, organisationIdentifier);
        String payload = buildEvents(validEvent);

        List<GoCardlessEvent> parsedEvents = parser.parse(payload);

        GoCardlessEvent firstEvent = parsedEvents.get(0);
        assertThat(firstEvent.getEventId(), is(nullValue()));
        assertThat(firstEvent.getId(), is(nullValue()));
        assertThat(firstEvent.getAction(), is(ACTION));
        assertThat(firstEvent.getResourceType(), is(RESOURCE_TYPE));
        assertThat(firstEvent.getCreatedAt(), is(CREATED_AT));
        assertThat(firstEvent.getOrganisationIdentifier(), is(organisationIdentifier));
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
        GoCardlessOrganisationId organisationIdentifier = GoCardlessOrganisationId.valueOf("test_organisation_identifier");

        String firstEventPayload = buildValidEvent(EVENT_ID, ACTION, RESOURCE_TYPE, "payment", CREATED_AT, organisationIdentifier);
        String secondEventPayload = buildValidEvent(secondEventId, secondEventAction, secondEventResourceType, "mandate", eventCreatedAt, organisationIdentifier);
        String thirdEventPayload = buildValidEvent(thirdEventId, thirdEventAction, thirdEventResourceType, "payout", eventCreatedAt, organisationIdentifier);
        String payload = buildEvents(firstEventPayload, secondEventPayload, thirdEventPayload);

        List<GoCardlessEvent> parsedEvents = parser.parse(payload);

        GoCardlessEvent firstEvent = parsedEvents.get(0);
        assertThat(firstEvent.getEventId(), is(nullValue()));
        assertThat(firstEvent.getId(), is(nullValue()));
        assertThat(firstEvent.getAction(), is(ACTION));
        assertThat(firstEvent.getResourceType(), is(RESOURCE_TYPE));
        assertThat(firstEvent.getCreatedAt(), is(CREATED_AT));
        assertThat(firstEvent.getOrganisationIdentifier(), is(organisationIdentifier));
        assertThat(firstEvent.getJson(), is(objectMapper.readTree(firstEventPayload).toString()));
        assertThat(firstEvent.getDetailsCause(), is("payment_confirmed"));
        assertThat(firstEvent.getDetailsDescription(), is("Payment was confirmed as collected"));
        assertThat(firstEvent.getDetailsOrigin(), is("gocardless"));
        assertThat(firstEvent.getDetailsReasonCode(), is("a reason code"));
        assertThat(firstEvent.getDetailsScheme(), is("a details scheme"));
        assertThat(firstEvent.getLinksNewCustomerBankAccount(), is("a bank account"));
        assertThat(firstEvent.getLinksNewMandate().get().toString(), is("a new mandate"));
        assertThat(firstEvent.getLinksOrganisation(), is(organisationIdentifier.toString()));
        assertThat(firstEvent.getLinksParentEvent(), is("a parent event"));
        assertThat(firstEvent.getLinksPreviousCustomerBankAccount(), is("a previous customer bank account"));
        assertThat(firstEvent.getLinksRefund(), is(""));
        assertThat(firstEvent.getLinksSubscription(), is("a subscription"));
        assertThat(firstEvent.getLinksPayment().get().toString(), is("payment"));
        assertThat(firstEvent.getLinksMandate(), is(Optional.empty()));
        assertThat(firstEvent.getLinksPayout(), is(nullValue()));
        
        GoCardlessEvent secondEvent = parsedEvents.get(1);
        assertThat(secondEvent.getEventId(), is(nullValue()));
        assertThat(secondEvent.getId(), is(nullValue()));
        assertThat(secondEvent.getAction(), is(secondEventAction));
        assertThat(secondEvent.getResourceType(), is(secondEventResourceType));
        assertThat(secondEvent.getCreatedAt(), is(eventCreatedAt));
        assertThat(secondEvent.getOrganisationIdentifier(), is(organisationIdentifier));
        assertThat(secondEvent.getJson(), is(objectMapper.readTree(secondEventPayload).toString()));
        assertThat(secondEvent.getLinksPayment(), is(Optional.empty()));
        assertThat(secondEvent.getLinksMandate().get().toString(), is("mandate"));
        assertThat(secondEvent.getLinksPayout(), is(nullValue()));


        GoCardlessEvent thirdEvent = parsedEvents.get(2);
        assertThat(thirdEvent.getEventId(), is(nullValue()));
        assertThat(thirdEvent.getId(), is(nullValue()));
        assertThat(thirdEvent.getAction(), is(thirdEventAction));
        assertThat(thirdEvent.getResourceType(), is(thirdEventResourceType));
        assertThat(thirdEvent.getCreatedAt(), is(eventCreatedAt));
        assertThat(thirdEvent.getOrganisationIdentifier(), is(organisationIdentifier));
        assertThat(thirdEvent.getJson(), is(objectMapper.readTree(thirdEventPayload).toString()));
        assertThat(thirdEvent.getLinksPayment(), is(Optional.empty()));
        assertThat(thirdEvent.getLinksMandate(), is(Optional.empty()));
        assertThat(thirdEvent.getLinksPayout(), is("payout"));
    }

    @Test
    public void shouldReturnEmptyListIfNoEventsAreThere() throws IOException {
        String payload = buildEvents();
        List<GoCardlessEvent> parsedEvents = parser.parse(payload);
        assertThat(parsedEvents.isEmpty(), is(true));
    }

    @Test
    public void shouldThrow_ifWebhookPayloadIsNotValid() {
        thrown.expect(WebhookParserException.class);
        thrown.expectMessage("Failed to parse webhooks, body: ");
        thrown.reportMissingExceptionWithMessage("WebhookParserException expected");
        parser.parse("");
    }
    
    private String buildValidEvent(String eventId, String action, GoCardlessResourceType resourceType, String resourceId,
                                   ZonedDateTime createdAt, GoCardlessOrganisationId organisationIdentifier) {
        return Json.createObjectBuilder()
                .add("id", eventId)
                .add("action", action)
                .add("created_at", createdAt.toString())
                .add("customer_notifications", 
                        Json.createObjectBuilder()
                                .add("deadline", "a deadline")
                                .add("id", "an id")
                                .add("mandatory", "true")
                                .add("type", "a type")
                                .build())
                .add("details",
                        Json.createObjectBuilder()
                                .add("cause", "payment_confirmed")
                                .add("description", "Payment was confirmed as collected")
                                .add("origin", "gocardless")
                                .add("reason_code", "a reason code")
                                .add("scheme", "a details scheme")
                                .build()
                )
                .add("metadata",
                        Json.createObjectBuilder()
                        .build())
                .add("resource_type", resourceType.toString().toLowerCase())
                .add("links", buildLinks(organisationIdentifier, resourceType, resourceId))
                .build().toString(); 
    }

    private JsonObject buildLinks(GoCardlessOrganisationId organisationId,
                                  GoCardlessResourceType goCardlessResourceType, String resourceId) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                .add("new_customer_bank_account", "a bank account")
                .add("new_mandate", "a new mandate")
                .add("organisation", organisationId.toString())
                .add("parent_event", "a parent event")
                .add("previous_customer_bank_account", "a previous customer bank account")
                .add("refund", "")
                .add("subscription", "a subscription");

        switch (goCardlessResourceType) {
            case PAYMENTS:
                jsonObjectBuilder.add("payment", resourceId);
                break;
            case MANDATES:
                jsonObjectBuilder.add("mandate", resourceId);
                break;
            case PAYOUTS:
                jsonObjectBuilder.add("payout", resourceId);
                break;
        }

        return jsonObjectBuilder.build();
    }

    private String buildEvents(String... events) throws IOException {
        ArrayNode eventsArray = objectMapper.createArrayNode();
        for (String event : events) {
            eventsArray.add(objectMapper.readTree(event));
        }
        ObjectNode eventsNode = objectMapper.createObjectNode();
        eventsNode.set("events", eventsArray);
        return eventsNode.toString();
    }
}
