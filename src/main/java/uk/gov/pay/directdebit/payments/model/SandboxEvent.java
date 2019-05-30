package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;

public class SandboxEvent {
    
    private Long mandateId;
    private Long paymentId;
    private String eventAction;
    private String eventCause;
    private ZonedDateTime createdAt;
    
    public SandboxEvent(Long mandateId, Long paymentId, String eventAction, String eventCause, ZonedDateTime createdAt) {
        this.mandateId = mandateId;
        this.paymentId = paymentId;
        this.eventAction = eventAction;
        this.eventCause = eventCause;
        this.createdAt = createdAt;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public SandboxEvent setMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public SandboxEvent setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    public String getEventAction() {
        return eventAction;
    }

    public SandboxEvent setEventAction(String eventAction) {
        this.eventAction = eventAction;
        return this;
    }

    public String getEventCause() {
        return eventCause;
    }

    public SandboxEvent setEventCause(String eventCause) {
        this.eventCause = eventCause;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public SandboxEvent setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
