package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
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
                GoCardlessEvent event = createGoCardlessEventFromJson(eventNode);
                events.add(event);
                LOGGER.info("Successfully parsed gocardless webhook, event resource type: {}, action: {}, resource id {}",
                        event.getResourceType(),
                        event.getAction(),
                        event.getResourceType());
            }
            return events;
        } catch (Exception exc) {
            throw new WebhookParserException("Failed to parse webhooks, body: " + webhookPayload);
        }
    }

    private GoCardlessEvent createGoCardlessEventFromJson(JsonNode eventNode) {
        GoCardlessEvent.GoCardlessEventBuilder eventBuilder = GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent()
                .withAction(getAsTextSafely(eventNode.get("action")))
                .withCreatedAt(ZonedDateTime.parse(getAsTextSafely(eventNode.get("created_at"))))
                .withCustomerId(getAsTextSafely(eventNode.get("customer_id")))
                .withDetailsCause(getAsTextSafely(eventNode.get("details.cause")))
                .withDetailsDescription(getAsTextSafely(eventNode.get("details.description")))
                .withDetailsOrigin(getAsTextSafely(eventNode.get("details.origin")))
                .withDetailsReasonCode(getAsTextSafely(eventNode.get("details.reason_code")))
                .withGoCardlessEventId(getAsTextSafely(eventNode.get("id")))
                .withId(getAsLongSafely(eventNode.get("id")))
                .withJson(eventNode.toString())
                .withResourceType(GoCardlessResourceType.fromString(getAsTextSafely(eventNode.get("resource_type"))));

        Optional.ofNullable(eventNode.get("details.scheme")).map(JsonNode::asText).ifPresent(eventBuilder::withDetailsScheme);
        getLinkField(eventNode, "mandate").ifPresent(eventBuilder::withMandateId);
        getLinkField(eventNode, "new_mandate").ifPresent(eventBuilder::withNewMandateId);
        getLinkField(eventNode, "organisation").map(PaymentProviderOrganisationIdentifier::of).ifPresent(eventBuilder::withOrganisationIdentifier);
        getLinkField(eventNode, "parent_event").ifPresent(eventBuilder::withParentEventId);
        getLinkField(eventNode, "payment").ifPresent(eventBuilder::withPaymentId);
        getLinkField(eventNode, "payout").ifPresent(eventBuilder::withPayoutId);
        getLinkField(eventNode, "previous_customer_bank_account").ifPresent(eventBuilder::withPreviousCustomerBankAccountId);
        getLinkField(eventNode, "refund").ifPresent(eventBuilder::withRefundId);
        getLinkField(eventNode, "subscription").ifPresent(eventBuilder::withSubscriptionId);
        return eventBuilder.build();
    }

    private Optional<String> getLinkField(JsonNode eventNode, String link) {
        return Optional.ofNullable(eventNode.get("links").get(link)).map(JsonNode::asText);
    }
    
    private String getAsTextSafely(JsonNode node) {
        if (node != null) {
            return node.asText();
        }
        return null;
    }
    
    private Long getAsLongSafely(JsonNode node) {
        if (node != null) {
            return node.asLong();
        }
        return null;
    }

    // There are actually more than these and we might want to store them in the interests of not throwing away data
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
