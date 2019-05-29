package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;

import java.time.ZonedDateTime;

public class GoCardlessEvent {

    private Long id;
    private Long eventId;
    private GoCardlessEventId goCardlessEventId;
    //todo action should be typed (see https://developer.gocardless.com/api-reference/#events-payment-actions and the equivalent for other resource_types
    private String action;
    private GoCardlessResourceType resourceType;
    private String resourceId;
    private String json;
    private String detailsCause;
    private String detailsDescription;
    private String detailsOrigin;
    private String detailsReasonCode;
    private String detailsScheme;
    private String linksMandate;
    private String linksNewCustomerBankAccount;
    private String linksNewMandate;
    private String linksOrganisation;
    private String linksParentEvent;
    private String linksPayment;
    private String linksPayout;
    private String linksPreviousCustomerBankAccount;
    private String linksRefund;
    private String linksSubscription;
    private ZonedDateTime createdAt;
    private GoCardlessOrganisationId organisationIdentifier;

    public GoCardlessEvent(Long id,
                           Long directDebitEventId,
                           GoCardlessEventId goCardlessEventId,
                           String action,
                           GoCardlessResourceType resourceType,
                           String json,
                           String detailsCause,
                           String detailsDescription,
                           String detailsOrigin,
                           String detailsReasonCode,
                           String detailsScheme,
                           String linksMandate,
                           String linksNewCustomerBankAccount,
                           String linksNewMandate,
                           String linksOrganisation,
                           String linksParentEvent,
                           String linksPayment,
                           String linksPayout,
                           String linksPreviousCustomerBankAccount,
                           String linksRefund,
                           String linksSubscription,
                           ZonedDateTime createdAt, GoCardlessOrganisationId organisationIdentifier) {
        this.id = id;
        this.eventId = directDebitEventId;
        this.goCardlessEventId = goCardlessEventId;
        this.action = action;
        this.resourceType = resourceType;
        this.json = json;
        this.detailsCause = detailsCause;
        this.detailsDescription = detailsDescription;
        this.detailsOrigin = detailsOrigin;
        this.detailsReasonCode = detailsReasonCode;
        this.detailsScheme = detailsScheme;
        this.linksMandate = linksMandate;
        this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
        this.linksNewMandate = linksNewMandate;
        this.linksOrganisation = linksOrganisation;
        this.linksParentEvent = linksParentEvent;
        this.linksPayment = linksPayment;
        this.linksPayout = linksPayout;
        this.linksPreviousCustomerBankAccount = linksPreviousCustomerBankAccount;
        this.linksRefund = linksRefund;
        this.linksSubscription = linksSubscription;
        this.createdAt = createdAt;
        this.organisationIdentifier = organisationIdentifier;
    }

    public GoCardlessEvent(Long id,
                           Long directDebitEventId,
                           GoCardlessEventId goCardlessEventId,
                           String action,
                           GoCardlessResourceType resourceType,
                           String json,
                           String detailsCause,
                           String detailsDescription,
                           String detailsOrigin,
                           String detailsReasonCode,
                           String detailsScheme,
                           String linksMandate,
                           String linksNewCustomerBankAccount,
                           String linksNewMandate,
                           String linksOrganisation,
                           String linksParentEvent,
                           String linksPayment,
                           String linksPayout,
                           String linksPreviousCustomerBankAccount,
                           String linksRefund,
                           String linksSubscription,
                           ZonedDateTime createdAt) {
        this(id, directDebitEventId, goCardlessEventId, action, resourceType, json, detailsCause, detailsDescription,
                detailsOrigin, detailsReasonCode, detailsScheme, linksMandate, linksNewCustomerBankAccount, 
                linksNewMandate, linksOrganisation, linksParentEvent, linksPayment, linksPayout, 
                linksPreviousCustomerBankAccount, linksRefund, linksSubscription, createdAt, null);
    }

    public GoCardlessEvent(GoCardlessEventId goCardlessEventId,
                           String action,
                           GoCardlessResourceType resourceType,
                           String json,
                           String detailsCause,
                           String detailsDescription,
                           String detailsOrigin,
                           String detailsReasonCode,
                           String detailsScheme,
                           String linksMandate,
                           String linksNewCustomerBankAccount,
                           String linksNewMandate,
                           String linksOrganisation,
                           String linksParentEvent,
                           String linksPayment,
                           String linksPayout,
                           String linksPreviousCustomerBankAccount,
                           String linksRefund,
                           String linksSubscription,
                           ZonedDateTime createdAt,
                           GoCardlessOrganisationId organisationIdentifier) {
        this(null, null, goCardlessEventId, action, resourceType, json, detailsCause, 
                detailsDescription, detailsOrigin, detailsReasonCode, detailsScheme, linksMandate,
                linksNewCustomerBankAccount, linksNewMandate, linksOrganisation, linksParentEvent, linksPayment,
                linksPayout, linksPreviousCustomerBankAccount, linksRefund, linksSubscription, createdAt, 
                organisationIdentifier);
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

    public GoCardlessEventId getGoCardlessEventId() {
        return goCardlessEventId;
    }

    public GoCardlessEvent setGoCardlessEventId(GoCardlessEventId goCardlessEventId) {
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

    public String getDetailsCause() {
        return detailsCause;
    }

    public GoCardlessEvent setDetailsCause(String detailsCause) {
        this.detailsCause = detailsCause;
        return this;
    }

    public String getDetailsDescription() {
        return detailsDescription;
    }

    public GoCardlessEvent setDetailsDescription(String detailsDescription) {
        this.detailsDescription = detailsDescription;
        return this;
    }

    public String getDetailsOrigin() {
        return detailsOrigin;
    }

    public GoCardlessEvent setDetailsOrigin(String detailsOrigin) {
        this.detailsOrigin = detailsOrigin;
        return this;
    }

    public String getDetailsReasonCode() {
        return detailsReasonCode;
    }

    public GoCardlessEvent setDetailsReasonCode(String detailsReasonCode) {
        this.detailsReasonCode = detailsReasonCode;
        return this;
    }

    public String getDetailsScheme() {
        return detailsScheme;
    }

    public GoCardlessEvent setDetailsScheme(String detailsScheme) {
        this.detailsScheme = detailsScheme;
        return this;
    }

    public String getLinksMandate() {
        return linksMandate;
    }

    public GoCardlessEvent setLinksMandate(String linksMandate) {
        this.linksMandate = linksMandate;
        return this;
    }

    public String getLinksNewCustomerBankAccount() {
        return linksNewCustomerBankAccount;
    }

    public GoCardlessEvent setLinksNewCustomerBankAccount(String linksNewCustomerBankAccount) {
        this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
        return this;
    }

    public String getLinksNewMandate() {
        return linksNewMandate;
    }

    public GoCardlessEvent setLinksNewMandate(String linksNewMandate) {
        this.linksNewMandate = linksNewMandate;
        return this;
    }

    public String getLinksOrganisation() {
        return linksOrganisation;
    }

    public GoCardlessEvent setLinksOrganisation(String linksOrganisation) {
        this.linksOrganisation = linksOrganisation;
        return this;
    }

    public String getLinksParentEvent() {
        return linksParentEvent;
    }

    public GoCardlessEvent setLinksParentEvent(String linksParentEvent) {
        this.linksParentEvent = linksParentEvent;
        return this;
    }

    public String getLinksPayment() {
        return linksPayment;
    }

    public GoCardlessEvent setLinksPayment(String linksPayment) {
        this.linksPayment = linksPayment;
        return this;
    }

    public String getLinksPayout() {
        return linksPayout;
    }

    public GoCardlessEvent setLinksPayout(String linksPayout) {
        this.linksPayout = linksPayout;
        return this;
    }

    public String getLinksPreviousCustomerBankAccount() {
        return linksPreviousCustomerBankAccount;
    }

    public GoCardlessEvent setLinksPreviousCustomerBankAccount(String linksPreviousCustomerBankAccount) {
        this.linksPreviousCustomerBankAccount = linksPreviousCustomerBankAccount;
        return this;
    }

    public String getLinksRefund() {
        return linksRefund;
    }

    public GoCardlessEvent setLinksRefund(String linksRefund) {
        this.linksRefund = linksRefund;
        return this;
    }

    public String getLinksSubscription() {
        return linksSubscription;
    }

    public GoCardlessEvent setLinksSubscription(String linksSubscription) {
        this.linksSubscription = linksSubscription;
        return this;
    }
}
