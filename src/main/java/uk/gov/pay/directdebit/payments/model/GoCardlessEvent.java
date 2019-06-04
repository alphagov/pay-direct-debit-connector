package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;

import java.time.ZonedDateTime;
import java.util.Objects;

public class GoCardlessEvent {

    private final Long id;
    private final String resourceId;
    private Long eventId;
    private final GoCardlessEventId goCardlessEventId;
    //todo action should be typed (see https://developer.gocardless.com/api-reference/#events-payment-actions and the equivalent for other resource_types
    private final String action;
    private final GoCardlessResourceType resourceType;
    private final String json;
    private final String detailsCause;
    private final String detailsDescription;
    private final String detailsOrigin;
    private final String detailsReasonCode;
    private final String detailsScheme;
    private final String linksMandate;
    private final String linksNewCustomerBankAccount;
    private final String linksNewMandate;
    private final String linksOrganisation;
    private final String linksParentEvent;
    private final String linksPayment;
    private final String linksPayout;
    private final String linksPreviousCustomerBankAccount;
    private final String linksRefund;
    private final String linksSubscription;
    private final ZonedDateTime createdAt;
    private final GoCardlessOrganisationId organisationIdentifier;

    private GoCardlessEvent(GoCardlessEventBuilder goCardlessEventBuilder) {
        this.id = goCardlessEventBuilder.id;
        this.resourceId = goCardlessEventBuilder.resourceId;
        this.eventId = goCardlessEventBuilder.eventId;
        this.goCardlessEventId = goCardlessEventBuilder.goCardlessEventId;
        this.action = goCardlessEventBuilder.action;
        this.resourceType = goCardlessEventBuilder.resourceType;
        this.json = goCardlessEventBuilder.json;
        this.detailsCause = goCardlessEventBuilder.detailsCause;
        this.detailsDescription = goCardlessEventBuilder.detailsDescription;
        this.detailsOrigin = goCardlessEventBuilder.detailsOrigin;
        this.detailsReasonCode = goCardlessEventBuilder.detailsReasonCode;
        this.detailsScheme = goCardlessEventBuilder.detailsScheme;
        this.linksMandate = goCardlessEventBuilder.linksMandate;
        this.linksNewCustomerBankAccount = goCardlessEventBuilder.linksNewCustomerBankAccount;
        this.linksNewMandate = goCardlessEventBuilder.linksNewMandate;
        this.linksOrganisation = goCardlessEventBuilder.linksOrganisation;
        this.linksParentEvent = goCardlessEventBuilder.linksParentEvent;
        this.linksPayment = goCardlessEventBuilder.linksPayment;
        this.linksPayout = goCardlessEventBuilder.linksPayout;
        this.linksPreviousCustomerBankAccount = goCardlessEventBuilder.linksPreviousCustomerBankAccount;
        this.linksRefund = goCardlessEventBuilder.linksRefund;
        this.linksSubscription = goCardlessEventBuilder.linksSubscription;
        this.createdAt = goCardlessEventBuilder.createdAt;
        this.organisationIdentifier = goCardlessEventBuilder.organisationIdentifier;
    }
    
    //TODO: This method will be removed once we stop creating generic events
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public GoCardlessEventId getGoCardlessEventId() {
        return goCardlessEventId;
    }

    public String getAction() {
        return action;
    }

    public GoCardlessResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getJson() {
        return json;
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

    public String getLinksMandate() {
        return linksMandate;
    }

    public String getLinksNewCustomerBankAccount() {
        return linksNewCustomerBankAccount;
    }

    public String getLinksNewMandate() {
        return linksNewMandate;
    }

    public String getLinksOrganisation() {
        return linksOrganisation;
    }

    public String getLinksParentEvent() {
        return linksParentEvent;
    }

    public String getLinksPayment() {
        return linksPayment;
    }

    public String getLinksPayout() {
        return linksPayout;
    }

    public String getLinksPreviousCustomerBankAccount() {
        return linksPreviousCustomerBankAccount;
    }

    public String getLinksRefund() {
        return linksRefund;
    }

    public String getLinksSubscription() {
        return linksSubscription;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoCardlessEvent that = (GoCardlessEvent) o;
        return id.equals(that.id) &&
                eventId.equals(that.eventId) &&
                goCardlessEventId.equals(that.goCardlessEventId) &&
                action.equals(that.action) &&
                resourceType == that.resourceType &&
                resourceId.equals(that.resourceId) &&
                json.equals(that.json) &&
                detailsCause.equals(that.detailsCause) &&
                detailsDescription.equals(that.detailsDescription) &&
                detailsOrigin.equals(that.detailsOrigin) &&
                detailsReasonCode.equals(that.detailsReasonCode) &&
                detailsScheme.equals(that.detailsScheme) &&
                linksMandate.equals(that.linksMandate) &&
                linksNewCustomerBankAccount.equals(that.linksNewCustomerBankAccount) &&
                linksNewMandate.equals(that.linksNewMandate) &&
                linksOrganisation.equals(that.linksOrganisation) &&
                linksParentEvent.equals(that.linksParentEvent) &&
                linksPayment.equals(that.linksPayment) &&
                linksPayout.equals(that.linksPayout) &&
                linksPreviousCustomerBankAccount.equals(that.linksPreviousCustomerBankAccount) &&
                linksRefund.equals(that.linksRefund) &&
                linksSubscription.equals(that.linksSubscription) &&
                createdAt.equals(that.createdAt) &&
                organisationIdentifier.equals(that.organisationIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, goCardlessEventId, action, resourceType, resourceId, json, detailsCause,
                detailsDescription, detailsOrigin, detailsReasonCode, detailsScheme, linksMandate,
                linksNewCustomerBankAccount, linksNewMandate, linksOrganisation, linksParentEvent,
                linksPayment, linksPayout, linksPreviousCustomerBankAccount, linksRefund, linksSubscription,
                createdAt, organisationIdentifier);
    }

    public GoCardlessOrganisationId getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public static final class GoCardlessEventBuilder {
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

        private GoCardlessEventBuilder() {
        }

        public static GoCardlessEventBuilder aGoCardlessEvent() {
            return new GoCardlessEventBuilder();
        }

        public GoCardlessEventBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public GoCardlessEventBuilder withEventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public GoCardlessEventBuilder withGoCardlessEventId(GoCardlessEventId goCardlessEventId) {
            this.goCardlessEventId = goCardlessEventId;
            return this;
        }

        public GoCardlessEventBuilder withAction(String action) {
            this.action = action;
            return this;
        }

        public GoCardlessEventBuilder withResourceType(GoCardlessResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public GoCardlessEventBuilder withResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public GoCardlessEventBuilder withJson(String json) {
            this.json = json;
            return this;
        }

        public GoCardlessEventBuilder withDetailsCause(String detailsCause) {
            this.detailsCause = detailsCause;
            return this;
        }

        public GoCardlessEventBuilder withDetailsDescription(String detailsDescription) {
            this.detailsDescription = detailsDescription;
            return this;
        }

        public GoCardlessEventBuilder withDetailsOrigin(String detailsOrigin) {
            this.detailsOrigin = detailsOrigin;
            return this;
        }

        public GoCardlessEventBuilder withDetailsReasonCode(String detailsReasonCode) {
            this.detailsReasonCode = detailsReasonCode;
            return this;
        }

        public GoCardlessEventBuilder withDetailsScheme(String detailsScheme) {
            this.detailsScheme = detailsScheme;
            return this;
        }

        public GoCardlessEventBuilder withLinksMandate(String linksMandate) {
            this.linksMandate = linksMandate;
            return this;
        }

        public GoCardlessEventBuilder withLinksNewCustomerBankAccount(String linksNewCustomerBankAccount) {
            this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
            return this;
        }

        public GoCardlessEventBuilder withLinksNewMandate(String linksNewMandate) {
            this.linksNewMandate = linksNewMandate;
            return this;
        }

        public GoCardlessEventBuilder withLinksOrganisation(String linksOrganisation) {
            this.linksOrganisation = linksOrganisation;
            return this;
        }

        public GoCardlessEventBuilder withLinksParentEvent(String linksParentEvent) {
            this.linksParentEvent = linksParentEvent;
            return this;
        }

        public GoCardlessEventBuilder withLinksPayment(String linksPayment) {
            this.linksPayment = linksPayment;
            return this;
        }

        public GoCardlessEventBuilder withLinksPayout(String linksPayout) {
            this.linksPayout = linksPayout;
            return this;
        }

        public GoCardlessEventBuilder withLinksPreviousCustomerBankAccount(String linksPreviousCustomerBankAccount) {
            this.linksPreviousCustomerBankAccount = linksPreviousCustomerBankAccount;
            return this;
        }

        public GoCardlessEventBuilder withLinksRefund(String linksRefund) {
            this.linksRefund = linksRefund;
            return this;
        }

        public GoCardlessEventBuilder withLinksSubscription(String linksSubscription) {
            this.linksSubscription = linksSubscription;
            return this;
        }

        public GoCardlessEventBuilder withCreatedAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GoCardlessEventBuilder withOrganisationIdentifier(GoCardlessOrganisationId organisationIdentifier) {
            this.organisationIdentifier = organisationIdentifier;
            return this;
        }

        public GoCardlessEvent build() {
            return new GoCardlessEvent(this);
        }
    }
}
