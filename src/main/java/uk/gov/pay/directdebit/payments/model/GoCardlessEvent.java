package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

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
    private PaymentProviderOrganisationIdentifier organisationIdentifier;
    private String detailsCause;
    private String detailsDescription;
    private String detailsOrigin;
    private String detailsReasonCode;
    private String detailsScheme;
    private String mandateId;
    private String customerId;
    private String newMandateId;
    private String parentEventId;
    private String paymentId;
    private String payoutId;
    private String previousCustomerBankAccount;
    private String refundId;
    private String subscriptionId;

    public GoCardlessEvent(Long id, Long eventId, String goCardlessEventId, String action, GoCardlessResourceType resourceType,
                           String json, ZonedDateTime createdAt, String resourceId,
                           PaymentProviderOrganisationIdentifier organisationIdentifier, String detailsCause,
                           String detailsDescription, String detailsOrigin, String detailsReasonCode, String detailsScheme,
                           String mandateId, String customerId, String newMandateId, String parentEventId,
                           String paymentId, String payoutId, String previousCustomerBankAccount, String refundId, String subscriptionId) {
        this.id = id;
        this.eventId = eventId;
        this.goCardlessEventId = goCardlessEventId;
        this.action = action;
        this.resourceType = resourceType;
        this.json = json;
        this.createdAt = createdAt;
        this.resourceId = resourceId;
        this.organisationIdentifier = organisationIdentifier;
        this.detailsCause = detailsCause;
        this.detailsDescription = detailsDescription;
        this.detailsOrigin = detailsOrigin;
        this.detailsReasonCode = detailsReasonCode;
        this.detailsScheme = detailsScheme;
        this.mandateId = mandateId;
        this.customerId = customerId;
        this.newMandateId = newMandateId;
        this.parentEventId = parentEventId;
        this.paymentId = paymentId;
        this.payoutId = payoutId;
        this.previousCustomerBankAccount = previousCustomerBankAccount;
        this.refundId = refundId;
        this.subscriptionId = subscriptionId;
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

    public PaymentProviderOrganisationIdentifier getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public void setOrganisationIdentifier(PaymentProviderOrganisationIdentifier organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }

    public String getDetailsCause() {
        return detailsCause;
    }

    public String getDetailsDescription() {
        return detailsDescription;
    }

    public String getDetailsOrigin() {
        return detailsOrigin;
    }

    public String getDetailsReasonCode() {
        return detailsReasonCode;
    }

    public String getDetailsScheme() {
        return detailsScheme;
    }

    public String getMandateId() {
        return mandateId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getNewMandateId() {
        return newMandateId;
    }

    public String getParentEventId() {
        return parentEventId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public String getPreviousCustomerBankAccount() {
        return previousCustomerBankAccount;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }
}
