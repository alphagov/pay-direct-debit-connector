package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitInfoFrontendResponse {

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

    @JsonProperty("mandate_reference")
    private String mandateReference;

    @JsonProperty("mandate_type")
    private String mandateType;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty
    private ExternalMandateState state;

    @JsonProperty("internal_state")
    private MandateState internalState;

    public DirectDebitInfoFrontendResponse(MandateExternalId paymentExternalId,
                                           Long gatewayAccountId,
                                           String gatewayAccountExternalId,
                                           MandateState internalState,
                                           String returnUrl,
                                           String mandateReference,
                                           String mandateType,
                                           String createdDate,
                                           Payer payer,
                                           Transaction transaction) {
        this.mandateExternalId = paymentExternalId.toString();
        this.internalState = internalState;
        this.state = internalState.toExternal();
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.returnUrl = returnUrl;
        this.mandateReference = mandateReference;
        this.mandateType = mandateType;
        this.createdDate = createdDate;
        this.transaction = initTransaction(transaction);
        this.payer = initPayer(payer);
    }

    private TransactionDetails initTransaction(Transaction transaction) {
        if (transaction != null) {
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
        if (payer != null) {
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

    public String getMandateReference() {
        return mandateReference;
    }

    public String getMandateType() {
        return mandateType;
    }

    public TransactionDetails getTransaction() {
        return transaction;
    }

    public PayerDetails getPayer() {
        return payer;
    }

    public String getMandateExternalId() {
        return mandateExternalId;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public ExternalMandateState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DirectDebitInfoFrontendResponse that = (DirectDebitInfoFrontendResponse) o;

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
        if (!mandateReference.equals(that.mandateReference)) {
            return false;
        }
        if (!mandateType.equals(that.mandateType)) {
            return false;
        }
        if (!createdDate.equals(that.createdDate)) {
            return false;
        }
        if (!internalState.equals(that.internalState)) {
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
        result = 31 * result + (mandateReference != null ? mandateReference.hashCode() : 0);
        result = 31 * result + mandateType.hashCode();
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + internalState.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DirectDebitInfoFrontendResponse{" +
                "payerId='" + payer.externalId + "'" +
                ", transactionId='" + transaction.externalId + "'" +
                ", mandateId='" + mandateExternalId + "'" +
                ", state='" + state.getState() + "'" +
                ", internalState='" + internalState + "'" +
                ", returnUrl='" + returnUrl + "'" +
                ", mandateReference='" + mandateReference + "'" +
                ", mandateType='" + mandateType + "'" +
                ", createdDate='" + createdDate + "'" +
                "}";
    }

}
