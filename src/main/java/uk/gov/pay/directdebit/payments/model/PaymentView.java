package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;

public class PaymentView {
    private String gatewayExternalId;
    private String paymentRequestExternalId;
    private Long amount;
    private String reference;
    private String description;
    private String returnUrl;
    private ZonedDateTime createdDate;
    private String name;
    private String email;
    private PaymentState state;

    public PaymentView(String gatewayExternalId,
                       String paymentRequestExternalId,
                       Long amount,
                       String reference,
                       String description,
                       String returnUrl,
                       ZonedDateTime createdDate,
                       String name,
                       String email,
                       PaymentState state) {
        this.gatewayExternalId = gatewayExternalId;
        this.paymentRequestExternalId = paymentRequestExternalId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
    }

    public String getGatewayExternalId() {
        return gatewayExternalId;
    }

    public String getPaymentRequestExternalId() {
        return paymentRequestExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentView that = (PaymentView) o;

        if (!gatewayExternalId.equals(that.gatewayExternalId)) return false;
        if (!paymentRequestExternalId.equals(that.paymentRequestExternalId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!returnUrl.equals(that.returnUrl)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equalsIgnoreCase(that.email) : that.email != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = gatewayExternalId.hashCode();
        result = 31 * result + paymentRequestExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
