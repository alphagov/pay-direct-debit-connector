package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class GoCardlessEvent implements Event {

    public static final String ACTION_MANDATE_SUBMITTED = "submitted";
    public static final String ACTION_MANDATE_ACTIVE = "active";
    public static final String ACTION_MANDATE_FAILED = "failed";
    public static final String ACTION_MANDATE_CANCELLED = "cancelled";
    public static final String ACTION_MANDATE_EXPIRED = "expired";
    public static final String ACTION_MANDATE_REINSTATED = "reinstated";
    public static final String ACTION_MANDATE_REPLACED = "replaced";

    public static final String ACTION_PAYMENT_SUBMITTED = "submitted";
    public static final String ACTION_PAYMENT_FAILED = "failed";
    public static final String ACTION_PAYMENT_PAID_OUT = "paid_out";
    public static final String ACTION_PAYMENT_CUSTOMER_APPROVAL_DENIED = "customer_approval_denied";
    public static final String ACTION_PAYMENT_CONFIRMED = "confirmed";
    public static final String ACTION_PAYMENT_CANCELLED = "cancelled";
    public static final String ACTION_PAYMENT_CHARGED_BACK = "charged_back";
    public static final String ACTION_PAYMENT_CHARGEBACK_SETTLED = "chargeback_settled";
    public static final String ACTION_PAYMENT_CHARGEBACK_CANCELLED = "chargeback_cancelled";
    public static final String ACTION_PAYMENT_LATE_FAILURE_SETTLED = "late_failure_settled";
    public static final String ACTION_PAYMENT_RESUBMISSION_REQUESTED = "resubmission_requested";

    private final Long id;
    private final String resourceId;
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
    private final GoCardlessMandateId linksMandate;
    private final String linksNewCustomerBankAccount;
    private final GoCardlessMandateId linksNewMandate;
    private final GoCardlessOrganisationId linksOrganisation;
    private final String linksParentEvent;
    private final GoCardlessPaymentId linksPayment;
    private final String linksPayout;
    private final String linksPreviousCustomerBankAccount;
    private final String linksRefund;
    private final String linksSubscription;
    private final ZonedDateTime createdAt;

    private GoCardlessEvent(GoCardlessEventBuilder goCardlessEventBuilder) {
        this.id = goCardlessEventBuilder.id;
        this.resourceId = goCardlessEventBuilder.resourceId;
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
    }
    
    public Long getId() {
        return id;
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

    public Optional<GoCardlessMandateId> getLinksMandate() {
        return Optional.ofNullable(linksMandate);
    }

    public String getLinksNewCustomerBankAccount() {
        return linksNewCustomerBankAccount;
    }

    public Optional<GoCardlessMandateId> getLinksNewMandate() {
        return Optional.ofNullable(linksNewMandate);
    }

    public GoCardlessOrganisationId getLinksOrganisation() {
        return linksOrganisation;
    }

    public String getLinksParentEvent() {
        return linksParentEvent;
    }

    public Optional<GoCardlessPaymentId> getLinksPayment() {
        return Optional.ofNullable(linksPayment);
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
    public ZonedDateTime getTimestamp() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoCardlessEvent that = (GoCardlessEvent) o;
        return id.equals(that.id) &&
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
                createdAt.equals(that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, goCardlessEventId, action, resourceType, resourceId, json, detailsCause,
                detailsDescription, detailsOrigin, detailsReasonCode, detailsScheme, linksMandate,
                linksNewCustomerBankAccount, linksNewMandate, linksOrganisation, linksParentEvent,
                linksPayment, linksPayout, linksPreviousCustomerBankAccount, linksRefund, linksSubscription,
                createdAt);
    }

    public static final class GoCardlessEventBuilder {
        private Long id;
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
        private GoCardlessMandateId linksMandate;
        private String linksNewCustomerBankAccount;
        private GoCardlessMandateId linksNewMandate;
        private GoCardlessOrganisationId linksOrganisation;
        private String linksParentEvent;
        private GoCardlessPaymentId linksPayment;
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

        public GoCardlessEventBuilder withLinksMandate(GoCardlessMandateId linksMandate) {
            this.linksMandate = linksMandate;
            return this;
        }

        public GoCardlessEventBuilder withLinksNewCustomerBankAccount(String linksNewCustomerBankAccount) {
            this.linksNewCustomerBankAccount = linksNewCustomerBankAccount;
            return this;
        }

        public GoCardlessEventBuilder withLinksNewMandate(GoCardlessMandateId linksNewMandate) {
            this.linksNewMandate = linksNewMandate;
            return this;
        }

        public GoCardlessEventBuilder withLinksOrganisation(GoCardlessOrganisationId linksOrganisation) {
            this.linksOrganisation = linksOrganisation;
            return this;
        }

        public GoCardlessEventBuilder withLinksParentEvent(String linksParentEvent) {
            this.linksParentEvent = linksParentEvent;
            return this;
        }

        public GoCardlessEventBuilder withLinksPayment(GoCardlessPaymentId linksPayment) {
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

        public GoCardlessEvent build() {
            return new GoCardlessEvent(this);
        }
    }
}
