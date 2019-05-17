package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;

import java.time.ZonedDateTime;

public class GoCardlessEvent {

    private Long id;
    private Long eventId;
    private String goCardlessEventId;
    //todo action should be typed (see https://developer.gocardless.com/api-reference/#events-payment-actions and the equivalent for other resource_types
    private String action;
    private GoCardlessResourceType resourceType;
    private String json;
    private ZonedDateTime createdAt;
    private String resourceId;
    private GoCardlessOrganisationId organisationIdentifier;

    public GoCardlessEvent(Long id,
                           Long directDebitEventId,
                           String goCardlessEventId,
                           String action,
                           GoCardlessResourceType resourceType,
                           String json,
                           ZonedDateTime createdAt,
                           GoCardlessOrganisationId organisationIdentifier) {
        this.id = id;
        this.eventId = directDebitEventId;
        this.goCardlessEventId = goCardlessEventId;
        this.action = action;
        this.resourceType = resourceType;
        this.json = json;
        this.createdAt = createdAt;
        this.organisationIdentifier = organisationIdentifier;
    }

    public GoCardlessEvent(Long id,
                           Long directDebitEventId,
                           String goCardlessEventId,
                           String action,
                           GoCardlessResourceType resourceType,
                           String json,
                           ZonedDateTime createdAt) {
        this(id, directDebitEventId, goCardlessEventId, action, resourceType, json, createdAt, null);
    }

    public GoCardlessEvent(String goCardlessEventId, 
                           String action, 
                           GoCardlessResourceType resourceType, 
                           String json, 
                           ZonedDateTime createdAt, 
                           GoCardlessOrganisationId organisationIdentifier) {
        this(null, null, goCardlessEventId, action, resourceType, json, createdAt, organisationIdentifier);
    }

    public Long getId() {
        return id;
    }

    public GoCardlessEvent setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getEventId() {
        return eventId;
    }

    public GoCardlessEvent setEventId(Long eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getGoCardlessEventId() {
        return goCardlessEventId;
    }

    public GoCardlessEvent setGoCardlessEventId(String goCardlessEventId) {
        this.goCardlessEventId = goCardlessEventId;
        return this;
    }

    public String getAction() {
        return action;
    }

    public GoCardlessEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public GoCardlessResourceType getResourceType() {
        return resourceType;
    }

    public GoCardlessEvent setResourceType(GoCardlessResourceType resourceType) {
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

    public GoCardlessOrganisationId getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public void setOrganisationIdentifier(GoCardlessOrganisationId organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }
}
