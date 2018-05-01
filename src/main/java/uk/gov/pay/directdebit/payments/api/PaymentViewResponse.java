package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResponse {

    @JsonProperty("gateway_external_id")
    private String gatewayExternalId;
    
    @JsonProperty("charge_id")
    private String paymentExternalId;
    
    @JsonProperty
    private Long amount;

    @JsonProperty("return_url")
    private String returnUrl;

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

    
    
    public PaymentViewResponse(String gatewayExternalId,
                               String paymentExternalId,
                               Long amount,
                               String reference,
                               String description,
                               String returnUrl,
                               String createdDate,
                               String name,
                               String email,
                               ExternalPaymentState state) {
        this.gatewayExternalId = gatewayExternalId;
        this.paymentExternalId = paymentExternalId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
    }

    public String getGatewayExternalId() { return gatewayExternalId; }

    public String getPaymentExternalId() { return paymentExternalId; }

    public Long getAmount() { return amount; }

    public String getReturnUrl() { return returnUrl; }

    public String getDescription() { return description; }

    public String getReference() { return reference; }

    public String getCreatedDate() { return createdDate; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public ExternalPaymentState getState() { return state; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentViewResponse that = (PaymentViewResponse) o;

        if (!gatewayExternalId.equals(that.gatewayExternalId)) return false;
        if (!paymentExternalId.equals(that.paymentExternalId)) return false;
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
        result = 31 * result + paymentExternalId.hashCode();
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

    @Override
    public String toString() {
        return "PaymentRequestResponse{" +
                "gatewayExternalId=" + gatewayExternalId +
                ", paymentRequestId='" + paymentExternalId + '\'' +
                ", amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                ", name=" + name +
                ", email=" + email +
                ", state='" + state.getState() + '\'' +
                '}';
    }
}
