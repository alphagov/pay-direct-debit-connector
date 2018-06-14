package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewListResponse {

    @JsonProperty("charge_id")
    private String transactionId;

    @JsonProperty
    private Long amount;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonProperty
    private ExternalPaymentState state;


    public PaymentViewListResponse(String transactionId,
                                   Long amount,
                                   String reference,
                                   String description,
                                   String createdDate,
                                   String name,
                                   String email,
                                   ExternalPaymentState state) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ExternalPaymentState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentViewListResponse that = (PaymentViewListResponse) o;

        if (!transactionId.equals(that.transactionId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equalsIgnoreCase(that.email) : that.email != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = transactionId.hashCode();
        result = 31 * result + transactionId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PaymentRequestResponse{" +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                ", name=" + name +
                ", email=" + email +
                ", state='" + state.getState() + '\'' +
                '}';
    }
}
