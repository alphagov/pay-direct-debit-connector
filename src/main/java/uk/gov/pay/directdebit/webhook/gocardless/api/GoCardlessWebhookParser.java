package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent.GoCardlessEventBuilder;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.GoCardlessEvent.GoCardlessEventBuilder.*;

public class GoCardlessWebhookParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessWebhookParser.class);

    private ObjectMapper objectMapper;

    @Inject
    GoCardlessWebhookParser(ObjectMapper objectMapper) {
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
                GoCardlessEventBuilder goCardlessEventBuilder = aGoCardlessEvent()
                        .withGoCardlessEventId(GoCardlessEventId.valueOf(eventNode.get("id").asText()))
                        .withAction(eventNode.get("action").asText())
                        .withJson(eventNode.toString())
                        .withCreatedAt(ZonedDateTime.parse(eventNode.get("created_at").asText()))
                        .withOrganisationIdentifier(getOrganisationField(eventNode));
                
                if (detailsNode.has("cause")) {
                    goCardlessEventBuilder.withDetailsCause(detailsNode.get("cause").asText());
                }
                if (detailsNode.has("description")) {
                    goCardlessEventBuilder.withDetailsDescription(detailsNode.get("description").asText());
                }
                if (detailsNode.has("origin")) {
                    goCardlessEventBuilder.withDetailsOrigin(detailsNode.get("origin").asText());
                }
                if (detailsNode.has("reason_code")) {
                    goCardlessEventBuilder.withDetailsReasonCode(detailsNode.get("reason_code").asText());
                }
                if (detailsNode.has("scheme")) {
                    goCardlessEventBuilder.withDetailsScheme(detailsNode.get("scheme").asText());

                }
                if (linksNode.has("mandate")) {
                    goCardlessEventBuilder.withLinksMandate(GoCardlessMandateId.valueOf(linksNode.get("mandate").asText()));
                }
                if (linksNode.has("new_customer_bank_account")) {
                    goCardlessEventBuilder
                            .withLinksNewCustomerBankAccount(linksNode.get("new_customer_bank_account").asText());
                }
                if (linksNode.has("new_mandate")) {
                    goCardlessEventBuilder.withLinksNewMandate(GoCardlessMandateId.valueOf(linksNode.get("new_mandate").asText()));
                }
                if (linksNode.has("organisation")) {
                    goCardlessEventBuilder.withLinksOrganisation(linksNode.get("organisation").asText());
                }
                if (linksNode.has("parent_event")) {
                    goCardlessEventBuilder.withLinksParentEvent(linksNode.get("parent_event").asText());
                }
                if (linksNode.has("payment")) {
                    goCardlessEventBuilder.withLinksPayment(linksNode.get("payment").asText());
                }
                if (linksNode.has("payout")) {
                    goCardlessEventBuilder.withLinksPayout(linksNode.get("payout").asText());
                }
                if (linksNode.has("previous_customer_bank_account")) {
                    goCardlessEventBuilder
                            .withLinksPreviousCustomerBankAccount(
                                    linksNode.get("previous_customer_bank_account").asText());
                }
                if (linksNode.has("refund")) {
                    goCardlessEventBuilder.withLinksRefund(linksNode.get("refund").asText());
                }
                if (linksNode.has("subscription")) {
                    goCardlessEventBuilder.withLinksSubscription(linksNode.get("subscription").asText());
                }
                
                GoCardlessResourceType handledGoCardlessResourceType =
                        GoCardlessResourceType.fromString(eventNode.get("resource_type").asText());
                goCardlessEventBuilder.withResourceType(handledGoCardlessResourceType);
                extractResourceIdFrom(eventNode, handledGoCardlessResourceType)
                        .ifPresent(goCardlessEventBuilder::withResourceId);
                GoCardlessEvent event = goCardlessEventBuilder.build();
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
