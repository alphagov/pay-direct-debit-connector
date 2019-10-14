package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class GovUkPayEvent implements Event {
    
    private final Long id;
    private final Long mandateId;
    private final Long paymentId;
    private final ZonedDateTime eventDate;
    private final ResourceType resourceType;
    private final GovUkPayEventType eventType;

    public GovUkPayEvent(Mandate mandate, GovUkPayEventType eventType) {
        this.mandateId = mandate.getId();
        this.resourceType = ResourceType.MANDATE;
        this.eventDate = ZonedDateTime.now(ZoneOffset.UTC);
        this.eventType = eventType;
        this.id = null;
        this.paymentId = null;
    }

    public GovUkPayEvent(Payment payment, GovUkPayEventType eventType) {
        this.paymentId = payment.getId();
        this.resourceType = ResourceType.PAYMENT;
        this.eventDate = ZonedDateTime.now(ZoneOffset.UTC);
        this.eventType = eventType;
        this.id = null;
        this.mandateId = null;
    }

    private GovUkPayEvent(GovUkPayEventBuilder builder) {
        this.id = builder.id;
        this.mandateId = builder.mandateId;
        this.paymentId = builder.paymentId;
        this.eventDate = Objects.requireNonNull(builder.eventDate);
        this.resourceType = Objects.requireNonNull(builder.resourceType);
        this.eventType = Objects.requireNonNull(builder.eventType);
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

    @Override
    public ZonedDateTime getTimestamp() {
        return eventDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GovUkPayEvent that = (GovUkPayEvent) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(mandateId, that.mandateId) &&
                Objects.equals(paymentId, that.paymentId) &&
                eventDate.equals(that.eventDate) &&
                resourceType == that.resourceType &&
                eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mandateId, paymentId, eventDate, resourceType, eventType);
    }

    @Override
    public String toString() {
        return "GovUkPayEvent{" +
                "id=" + id +
                ", mandateId=" + mandateId +
                ", paymentId=" + paymentId +
                ", eventDate=" + eventDate +
                ", resourceType=" + resourceType +
                ", eventType=" + eventType +
                '}';
    }

    public enum ResourceType {
        PAYMENT,
        MANDATE
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

        public GovUkPayEventBuilder withMandateId(Long mandateId) {
            if (mandateId != null) {
                this.mandateId = mandateId;
            }
            return this;
        }

        public GovUkPayEventBuilder withPaymentId(Long paymentId) {
            if (paymentId != null) {
                this.paymentId = paymentId;
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
            return new GovUkPayEvent(this);
        }
    }
}
