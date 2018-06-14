package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public class TransactionDetails {
        @JsonProperty("external_id")
        private String externalId;
        private Long amount;
        private ExternalPaymentState state;
        private String description;
        private String reference;

        public TransactionDetails(String externalId, Long amount,
                ExternalPaymentState state, String description, String reference) {
            this.externalId = externalId;
            this.amount = amount;
            this.state = state;
            this.description = description;
            this.reference = reference;
        }

        public String getExternalId() {
            return externalId;
        }

        public Long getAmount() {
            return amount;
        }

        public ExternalPaymentState getState() {
            return state;
        }

        public String getDescription() {
            return description;
        }

        public String getReference() {
            return reference;
        }
    }

    @JsonProperty("transaction")
    private TransactionDetails transaction;
    
    @JsonProperty("external_id")
    private String mandateExternalId;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;

    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;
    
    @JsonProperty
    private String reference;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalMandateState state;

    public PaymentRequestFrontendResponse(String paymentExternalId, Long gatewayAccountId, String gatewayAccountExternalId, ExternalMandateState state, String returnUrl, String reference, String createdDate, Payer payer, Transaction transaction) {
        this.mandateExternalId = paymentExternalId;
        this.state = state;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.returnUrl = returnUrl;
        this.reference = reference;
        this.createdDate = createdDate;
        this.transaction = initTransaction(transaction);
        this.payer = initPayer(payer);
    }

    private TransactionDetails initTransaction(Transaction transaction) {
        if (transaction != null ) {
            return new TransactionDetails(
                    transaction.getExternalId(),
                    transaction.getAmount(),
                    transaction.getState().toExternal(),
                    transaction.getDescription(),
                    transaction.getReference()
            );
        }
        return null;
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
    public String getReturnUrl() {
        return returnUrl;
    }

    public String getReference() {
        return reference;
    }

    public TransactionDetails getTransaction() {
        return transaction;
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
        if (transaction != null ? !transaction.equals(that.transaction) : that.transaction != null) {
            return false;
        }
        if (!mandateExternalId.equals(that.mandateExternalId)) {
            return false;
        }
        if (!returnUrl.equals(that.returnUrl)) {
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
        result = 31 * result + (transaction != null ? transaction.hashCode() : 0);
        result = 31 * result + mandateExternalId.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PaymentRequestResponse{" +
                "payer=" + payer.externalId +
                ", transaction='" + transaction.externalId + '\'' +
                ", paymentRequestId='" + mandateExternalId + '\'' +
                ", state='" + state.getState() + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }

}


