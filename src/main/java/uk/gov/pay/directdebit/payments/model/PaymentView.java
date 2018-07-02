package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;

public class PaymentView {
    private String gatewayExternalId;
    private String transactionExternalId;
    private Long amount;
    private String reference;
    private String description;
    private ZonedDateTime createdDate;
    private String name;
    private String email;
    private PaymentState state;
    private String mandateExternalId;

    public PaymentView(String gatewayExternalId,
                       String transactionExternalId,
                       Long amount,
                       String reference,
                       String description,
                       ZonedDateTime createdDate,
                       String name,
                       String email,
                       PaymentState state,
                       String mandateExternalId) {
        this.gatewayExternalId = gatewayExternalId;
        this.transactionExternalId = transactionExternalId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
        this.mandateExternalId = mandateExternalId;
    }

    public String getGatewayExternalId() {
        return gatewayExternalId;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
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

    public String getMandateExternalId() { return mandateExternalId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentView that = (PaymentView) o;

        if (!gatewayExternalId.equals(that.gatewayExternalId)) return false;
        if (!transactionExternalId.equals(that.transactionExternalId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equalsIgnoreCase(that.email) : that.email != null) return false;
        if (mandateExternalId != null ? !mandateExternalId.equalsIgnoreCase(that.mandateExternalId) : that.mandateExternalId != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = gatewayExternalId.hashCode();
        result = 31 * result + transactionExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + state.hashCode();
        result = 31 * result + (mandateExternalId != null ? mandateExternalId.hashCode() : 0);
        return result;
    }
}
