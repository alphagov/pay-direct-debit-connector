package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoCardlessWebhookParser {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessWebhookParser.class);

    private ObjectMapper objectMapper;

    @Inject
    public GoCardlessWebhookParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<GoCardlessEvent> parse(String webhookPayload) {
        try {
            List<GoCardlessEvent> events = new ArrayList<>();
            JsonNode webhookJson = objectMapper.readTree(webhookPayload);
            JsonNode eventsPayload = webhookJson.get("events");
            for (JsonNode eventNode : eventsPayload) {
                String resourceType = eventNode.get("resource_type").asText();
                GoCardlessResourceType handledGoCardlessResourceType = GoCardlessResourceType.fromString(resourceType);
                GoCardlessEvent event = new GoCardlessEvent(
                        eventNode.get("id").asText(),
                        eventNode.get("action").asText(),
                        handledGoCardlessResourceType,
                        eventNode.toString(),
                        ZonedDateTime.parse(eventNode.get("created_at").asText()),
                        getOrganisationField(eventNode)
                );
                extractResourceIdFrom(eventNode, handledGoCardlessResourceType)
                        .ifPresent(event::setResourceId);
                events.add(event);
                LOGGER.info("Successfully parsed gocardless webhook, event resource type: {}, action: {}, resource id {}",
                        event.getResourceType(),
                        event.getAction(),
                        event.getResourceId());
            }
            return events;
        } catch (Exception exc) {
            throw new WebhookParserException("Failed to parse webhooks, body: " + webhookPayload);
        }
    }

    private PaymentProviderOrganisationIdentifier getOrganisationField(JsonNode eventNode) {
        /* todo: remove the check for missing node after going live
        Now is used for backward compatibility as when live we will get the organisation in the payload
        */
        if (eventNode.get("links").has("organisation")) {
            return PaymentProviderOrganisationIdentifier.of(eventNode.get("links").get("organisation").asText());
        } else {
            return null;
        }
    }

    private Optional<String> extractResourceIdFrom(JsonNode jsonNode, GoCardlessResourceType goCardlessResourceType) {
        switch (goCardlessResourceType) {
            case PAYMENTS:
                return Optional.of(jsonNode.get("links").get("payment").asText());
            case MANDATES:
                return Optional.of(jsonNode.get("links").get("mandate").asText());
            case PAYOUTS:
                return Optional.of(jsonNode.get("links").get("payout").asText());
            default:
                return Optional.empty();
        }
    }
}
