package uk.gov.pay.directdebit.events.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventTypeException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class GovUkPayEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkPayEvent.class);

    private final Long id;
    private final Long mandateId;
    private final Long paymentId;
    private final ZonedDateTime eventDate;
    private final ResourceType resourceType;
    private final GovUkPayEventType eventType;

    public GovUkPayEvent(Long id, Long mandateId, Long paymentId, ZonedDateTime eventDate, ResourceType resourceType, GovUkPayEventType eventType) {
        this.id = id;
        this.mandateId = mandateId;
        this.paymentId = paymentId;
        this.eventDate = Objects.requireNonNull(eventDate);
        this.resourceType = Objects.requireNonNull(resourceType);
        this.eventType = Objects.requireNonNull(eventType);
    }

    public Long getId() {
        return id;
    }

    public Optional<Long> getMandateId() {
        return Optional.ofNullable(mandateId);
    }

    public Optional<Long> getPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public GovUkPayEventType getEventType() {
        return eventType;
    }

    public enum ResourceType {
        PAYMENT,
        MANDATE
    }
    
    public enum GovUkPayEventType {
        MANDATE_CREATED,
        MANDATE_TOKEN_EXCHANGED,
        MANDATE_SUBMITTED,
        MANDATE_EXPIRED_BY_SYSTEM,
        MANDATE_CANCELLED_BY_USER,
        MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE,
        PAYMENT_SUBMITTED;

        public static GovUkPayEventType fromString(String event) {
            try {
                return valueOf(event);
            } catch (Exception e) {
                LOGGER.error("Tried to parse unknown event {}", event);
                throw new InvalidGovUkPayEventTypeException(event);
            }
        }
    }

    public static final class GovUkPayEventBuilder {
        private Long id;
        private Long mandateId;
        private Long paymentId;
        private ZonedDateTime eventDate;
        private ResourceType resourceType;
        private GovUkPayEventType eventType;

        private GovUkPayEventBuilder() {
        }

        public static GovUkPayEventBuilder aGovUkPayEvent() {
            return new GovUkPayEventBuilder();
        }

        public GovUkPayEventBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public GovUkPayEventBuilder withMandate(Mandate mandate) {
            if (mandate != null) {
                this.mandateId = mandate.getId();
            }
            return this;
        }

        public GovUkPayEventBuilder withPayment(Payment payment) {
            if (payment != null) {
                this.paymentId = payment.getId();
            }
            return this;
        }

        public GovUkPayEventBuilder withEventDate(ZonedDateTime eventDate) {
            this.eventDate = eventDate;
            return this;
        }

        public GovUkPayEventBuilder withResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public GovUkPayEventBuilder withEventType(GovUkPayEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public GovUkPayEvent build() {
            return new GovUkPayEvent(id, mandateId, paymentId, eventDate, resourceType, eventType);
        }
    }
}
