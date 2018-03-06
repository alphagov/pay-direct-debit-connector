package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;

public class GoCardlessEvent {

    private Long id;
    private Long paymentRequestEventId;
    private String eventId;
    private String action;
    private String resourceType;
    private String json;
    private ZonedDateTime createdAt;
    private String resourceId;

    public GoCardlessEvent(Long id, Long paymentRequestEventId, String eventId, String action, String resourceType, String json, ZonedDateTime createdAt) {
        this.id = id;
        this.paymentRequestEventId = paymentRequestEventId;
        this.eventId = eventId;
        this.action = action;
        this.resourceType = resourceType;
        this.json = json;
        this.createdAt = createdAt;
    }

    public GoCardlessEvent(Long paymentRequestEventId, String eventId, String action, String resourceType, String json, ZonedDateTime createdAt) {
        this(null, paymentRequestEventId, eventId, action,  resourceType, json, createdAt);
    }

    public Long getId() {
        return id;
    }

    public GoCardlessEvent setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getPaymentRequestEventId() {
        return paymentRequestEventId;
    }

    public GoCardlessEvent setPaymentRequestEventId(Long paymentRequestEventId) {
        this.paymentRequestEventId = paymentRequestEventId;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public GoCardlessEvent setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getAction() {
        return action;
    }

    public GoCardlessEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public GoCardlessEvent setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public GoCardlessEvent withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public GoCardlessEvent setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getJson() {
        return json;
    }

    public GoCardlessEvent setJson(String json) {
        this.json = json;
        return this;
    }

    public GoCardlessEvent setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }
}
