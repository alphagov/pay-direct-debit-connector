package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoCardlessWebhookParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessWebhookParser.class);

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
                JsonNode detailsNode = eventNode.get("details");
                JsonNode linksNode = eventNode.get("links");
                String detailsCause =  
                        detailsNode.has("cause") ? detailsNode.get("cause").asText() : null;
                String detailsDescription =
                        detailsNode.has("description") ? detailsNode.get("description").asText() : null;
                String detailsOrigin =
                        detailsNode.has("origin") ? detailsNode.get("origin").asText() :null;
                String detailsReasonCode =
                        detailsNode.has("reason_code") ? detailsNode.get("reason_code").asText() :null;
                String detailsScheme =
                        detailsNode.has("scheme") ? detailsNode.get("scheme").asText() :null;
                String linksMandate 
                        = detailsNode.has("mandate") ? linksNode.get("mandate").asText() : null;
                String linksNewCustomerBankAccount 
                        = detailsNode.has("new_customer_bank_account") 
                        ? linksNode.get("new_customer_bank_account").asText() : null;
                String linksNewMandate 
                        = detailsNode.has("new_mandate") ? linksNode.get("new_mandate").asText() : null;
                String linksOrganisation
                        = detailsNode.has("organisation") ? linksNode.get("organisation").asText() : null;
                String linksParentEvent 
                        = detailsNode.has("parent_event") ? linksNode.get("parent_event").asText() : null;
                String linksPayment 
                        = detailsNode.has("payment") ? linksNode.get("payment").asText() : null;
                String linksPayout 
                        = detailsNode.has("payout") ? linksNode.get("payout").asText() : null;
                String linksPreviousCustomerBankAccount = detailsNode.has("previous_customer_bank_account") 
                        ? linksNode.get("previous_customer_bank_account").asText() : null;
                String linksRefund 
                        = detailsNode.has("refund") ? linksNode.get("refund").asText() : null;
                String linksSubscription = detailsNode.has("subscription") 
                        ? linksNode.get("subscription").asText() : null;
                        String resourceType = eventNode.get("resource_type").asText();
                GoCardlessResourceType handledGoCardlessResourceType = GoCardlessResourceType.fromString(resourceType);
                GoCardlessEvent event = new GoCardlessEvent(
                        GoCardlessEventId.valueOf(eventNode.get("id").asText()),
                        eventNode.get("action").asText(),
                        handledGoCardlessResourceType,
                        eventNode.toString(),
                        detailsCause,
                        detailsDescription,
                        detailsOrigin,
                        detailsReasonCode,
                        detailsScheme,
                        linksMandate,
                        linksNewCustomerBankAccount,
                        linksNewMandate,
                        linksOrganisation,
                        linksParentEvent,
                        linksPayment,
                        linksPayout,
                        linksPreviousCustomerBankAccount,
                        linksRefund,
                        linksSubscription,
                        ZonedDateTime.parse(eventNode.get("created_at").asText()),
                        getOrganisationField(eventNode));
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

    private GoCardlessOrganisationId getOrganisationField(JsonNode eventNode) {
        /* todo: remove the check for missing node after going live
        Now is used for backward compatibility as when live we will get the organisation in the payload
        */
        if (eventNode.get("links").has("organisation")) {
            return GoCardlessOrganisationId.valueOf(eventNode.get("links").get("organisation").asText());
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
