package uk.gov.pay.directdebit.events.model;

import java.time.ZonedDateTime;

public class SandboxEvent {
    
    private final String mandateId;
    private final String paymentId;
    private final String eventAction;
    private final String eventCause;
    private final ZonedDateTime createdAt;
    
    public SandboxEvent(String mandateId, String paymentId, String eventAction, String eventCause, ZonedDateTime createdAt) {
        this.mandateId = mandateId;
        this.paymentId = paymentId;
        this.eventAction = eventAction;
        this.eventCause = eventCause;
        this.createdAt = createdAt;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getPaymentId() {
        return paymentId;
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
}
