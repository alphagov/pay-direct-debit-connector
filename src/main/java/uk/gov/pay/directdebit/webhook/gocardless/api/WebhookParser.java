package uk.gov.pay.directdebit.webhook.gocardless.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.webhook.gocardless.exception.WebhookParserException;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class WebhookParser {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookParser.class);

    private ObjectMapper objectMapper;

    @Inject
    public WebhookParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public enum HandledResourceType {
        PAYMENTS, MANDATES;

        public static HandledResourceType fromString(String type) {
            for (HandledResourceType typeEnum : HandledResourceType.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            LOGGER.warn("Received webhook from gocardless with unhandled resource_type: {}", type);
            return null;
        }
    }

    public List<GoCardlessEvent> parse(String webhookPayload) {
        try {
            List<GoCardlessEvent> events = new ArrayList<>();
            JsonNode webhookJson = objectMapper.readTree(webhookPayload);
            JsonNode eventsPayload = webhookJson.get("events");
            for (JsonNode eventNode: eventsPayload) {
                String resourceType = eventNode.get("resource_type").asText();
                HandledResourceType handledResourceType = HandledResourceType.fromString(resourceType);
                String bla = null;

                if (handledResourceType != null) {
                    switch (handledResourceType) {
                        case PAYMENTS:
                            bla = eventNode.get("links").get("payment").asText();
                            break;
                        case MANDATES:
                            bla = eventNode.get("links").get("mandate").asText();
                            break;
                    }
                }
                GoCardlessEvent event = new GoCardlessEvent(
                        null,
                        eventNode.get("id").asText(),
                        eventNode.get("action").asText(),
                        resourceType,
                        eventNode.toString(),
                        ZonedDateTime.parse(eventNode.get("created_at").asText())
                );
                event.setResourceId(bla);
                events.add(event);
            }
            return events;
        } catch (Exception exc) {
            throw new WebhookParserException("Failed to parse webhooks, body: " + webhookPayload);
        }
    }
}
