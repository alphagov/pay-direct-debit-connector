package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

import java.time.ZonedDateTime;

public class GoCardlessEvent {

    private Long id;
    private String goCardlessEventId;
    //todo action should be typed (see https://developer.gocardless.com/api-reference/#events-payment-actions and the equivalent for other resource_types
    private String action;
    private GoCardlessResourceType resourceType;
    private String json;
    private ZonedDateTime createdAt;
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

    private GoCardlessEvent(Long id, String goCardlessEventId, String action, GoCardlessResourceType resourceType,
                            String json, ZonedDateTime createdAt,
                            PaymentProviderOrganisationIdentifier organisationIdentifier, String detailsCause,
                            String detailsDescription, String detailsOrigin, String detailsReasonCode, String detailsScheme,
                            String mandateId, String customerId, String newMandateId, String parentEventId,
                            String paymentId, String payoutId, String previousCustomerBankAccount, String refundId, String subscriptionId) {
        this.id = id;
        this.goCardlessEventId = goCardlessEventId;
        this.action = action;
        this.resourceType = resourceType;
        this.json = json;
        this.createdAt = createdAt;
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

    public GoCardlessEvent withResourceId(String resourceId) {
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


    public static final class GoCardlessEventBuilder {
        private Long id;
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

        private GoCardlessEventBuilder() {
        }

        public static GoCardlessEventBuilder aGoCardlessEvent() {
            return new GoCardlessEventBuilder();
        }

        public GoCardlessEventBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public GoCardlessEventBuilder withGoCardlessEventId(String goCardlessEventId) {
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

        public GoCardlessEventBuilder withJson(String json) {
            this.json = json;
            return this;
        }

        public GoCardlessEventBuilder withCreatedAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GoCardlessEventBuilder withResourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public GoCardlessEventBuilder withOrganisationIdentifier(PaymentProviderOrganisationIdentifier organisationIdentifier) {
            this.organisationIdentifier = organisationIdentifier;
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

        public GoCardlessEventBuilder withMandateId(String mandateId) {
            this.mandateId = mandateId;
            return this;
        }

        public GoCardlessEventBuilder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public GoCardlessEventBuilder withNewMandateId(String newMandateId) {
            this.newMandateId = newMandateId;
            return this;
        }

        public GoCardlessEventBuilder withParentEventId(String parentEventId) {
            this.parentEventId = parentEventId;
            return this;
        }

        public GoCardlessEventBuilder withPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public GoCardlessEventBuilder withPayoutId(String payoutId) {
            this.payoutId = payoutId;
            return this;
        }

        public GoCardlessEventBuilder withPreviousCustomerBankAccountId(String previousCustomerBankAccount) {
            this.previousCustomerBankAccount = previousCustomerBankAccount;
            return this;
        }

        public GoCardlessEventBuilder withRefundId(String refundId) {
            this.refundId = refundId;
            return this;
        }

        public GoCardlessEventBuilder withSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public GoCardlessEvent build() {
            return new GoCardlessEvent(id, goCardlessEventId, action, resourceType, json, createdAt, organisationIdentifier, detailsCause, detailsDescription, detailsOrigin, detailsReasonCode, detailsScheme, mandateId, customerId, newMandateId, parentEventId, paymentId, payoutId, previousCustomerBankAccount, refundId, subscriptionId);
        }
    }
}
