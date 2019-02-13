package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.payments.links.Link;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResultResponse {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty
    private Long amount;

    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private ZonedDateTime createdDate;

    @JsonProperty("agreement_id")
    private String mandateExternalId;

    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonProperty
    private ExternalPaymentState state;

    @JsonProperty
    private List<Link> links = new ArrayList<>();


    public PaymentViewResultResponse(String transactionId,
                                     Long amount,
                                     String reference,
                                     String description,
                                     ZonedDateTime createdDate,
                                     String name,
                                     String email,
                                     ExternalPaymentState state,
                                     String mandateExternalId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
        this.mandateExternalId = mandateExternalId;
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

    public ZonedDateTime getCreatedDate() {
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

    public List<Link> getLinks() {
        return links;
    }

    public PaymentViewResultResponse withLink(Link link) {
        this.links.add(link);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentViewResultResponse that = (PaymentViewResultResponse) o;

        if (!transactionId.equals(that.transactionId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) return false;
        if (!createdDate.equals(that.createdDate)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (email != null ? !email.equalsIgnoreCase(that.email) : that.email != null) return false;
        if (mandateExternalId != null ? !mandateExternalId.equalsIgnoreCase(that.mandateExternalId) : that.mandateExternalId != null)
            return false;
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
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + state.hashCode();
        result = 31 * result + (mandateExternalId != null ? mandateExternalId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
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
