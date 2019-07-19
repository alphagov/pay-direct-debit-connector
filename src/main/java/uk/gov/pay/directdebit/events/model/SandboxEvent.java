package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.time.ZonedDateTime;
import java.util.Optional;

public class SandboxEvent implements Event {
    
    private final SandboxMandateId mandateId;
    private final SandboxPaymentId paymentId;
    private final String eventAction;
    private final String eventCause;
    private final ZonedDateTime createdAt;
    
    private SandboxEvent(SandboxEventBuilder builder) {
        this.mandateId = builder.mandateId;
        this.paymentId = builder.paymentId;
        this.eventAction = builder.eventAction;
        this.eventCause = builder.eventCause;
        this.createdAt = builder.createdAt;
    }

    public Optional<SandboxMandateId> getMandateId() {
        return Optional.ofNullable(mandateId);
    }

    public Optional<SandboxPaymentId> getPaymentId() {
        return Optional.ofNullable(paymentId);
    }

    public String getEventAction() {
        return eventAction;
    }

    public String getEventCause() {
        return eventCause;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return createdAt;
    }

    public static final class SandboxEventBuilder {
        private SandboxMandateId mandateId;
        private SandboxPaymentId paymentId;
        private String eventAction;
        private String eventCause;
        private ZonedDateTime createdAt;

        private SandboxEventBuilder() {
        }

        public static SandboxEventBuilder aSandboxEvent() {
            return new SandboxEventBuilder();
        }

        public SandboxEventBuilder withMandateId(SandboxMandateId mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public SandboxEventBuilder withPaymentId(SandboxPaymentId paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public SandboxEventBuilder withEventAction(String eventAction) {
            this.eventAction = eventAction;
            return this;
        }

        public SandboxEventBuilder withEventCause(String eventCause) {
            this.eventCause = eventCause;
            return this;
        }

        public SandboxEventBuilder withCreatedAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SandboxEvent build() {
            return new SandboxEvent(this);
        }
    }
}
