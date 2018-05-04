package uk.gov.pay.directdebit.payments.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.model.Payer;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentRequestFrontendResponse {

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public static class PayerDetails {
        @JsonProperty("payer_external_id")
        private String externalId;
        
        @JsonProperty("account_holder_name")
        private String name;
        
        @JsonProperty
        private String email;

        @JsonProperty("requires_authorisation")
        private boolean accountRequiresAuthorisation;

        PayerDetails(String externalId, String name, String email, boolean accountRequiresAuthorisation) {
            this.externalId = externalId;
            this.name = name;
            this.email = email;
            this.accountRequiresAuthorisation = accountRequiresAuthorisation;
        }

        public String getExternalId() {
            return externalId;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public boolean getAccountRequiresAuthorisation() {
            return accountRequiresAuthorisation;
        }

        @Override
        public String toString() {
            return "PayerDetails{" +
                    "externalId='" + externalId + '\'' +
                    '}';
        }
    }

    @JsonProperty("payer")
    private PayerDetails payer;
    
    @JsonProperty("external_id")
    private String paymentExternalId;

    @JsonProperty
    private Long amount;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;

    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;
    
    @JsonProperty
    private String description;

    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalPaymentState state;

    public PaymentRequestFrontendResponse(String paymentExternalId, Long gatewayAccountId, String gatewayAccountExternalId, ExternalPaymentState state, Long amount, String returnUrl, String description, String reference, String createdDate, Payer payer) {
        this.paymentExternalId = paymentExternalId;
        this.state = state;
        this.amount = amount;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        this.payer = initPayer(payer);
    }

    private PayerDetails initPayer(Payer payer) {
        if (payer != null ) {
            return new PayerDetails(
                    payer.getExternalId(),
                    payer.getName(),
                    payer.getEmail(),
                    payer.getAccountRequiresAuthorisation());
        } 
        return null;
    }
    public PayerDetails getPayerDetails() {
        return payer;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public String getPaymentExternalId() {
        return paymentExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentRequestFrontendResponse that = (PaymentRequestFrontendResponse) o;

        if (payer != null ? !payer.equals(that.payer) : that.payer != null) {
            return false;
        }
        if (!paymentExternalId.equals(that.paymentExternalId)) {
            return false;
        }
        if (!amount.equals(that.amount)) {
            return false;
        }
        if (!returnUrl.equals(that.returnUrl)) {
            return false;
        }
        if (!description.equals(that.description)) {
            return false;
        }
        if (!reference.equals(that.reference)) {
            return false;
        }
        if (!createdDate.equals(that.createdDate)) {
            return false;
        }
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = payer != null ? payer.hashCode() : 0;
        result = 31 * result + paymentExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + reference.hashCode();
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PaymentRequestResponse{" +
                "payer=" + payer +
                ", paymentRequestId='" + paymentExternalId + '\'' +
                ", state='" + state.getState() + '\'' +
                ", amount=" + amount +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

}


