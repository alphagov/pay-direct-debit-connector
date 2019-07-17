package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.api.json.ApiResponseDateTimeSerializer;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class DirectDebitInfoFrontendResponse {

    @JsonProperty("payer")
    private final PayerDetails payer;

    @JsonProperty("payment")
    private final PaymentDetails payment;

    @JsonProperty("external_id")
    private final String mandateExternalId;

    @JsonProperty("return_url")
    private final String returnUrl;

    @JsonProperty("gateway_account_id")
    private final Long gatewayAccountId;

    @JsonProperty("gateway_account_external_id")
    private final String gatewayAccountExternalId;

    @JsonProperty("mandate_reference")
    @JsonSerialize(using = ToStringSerializer.class)
    private final MandateBankStatementReference mandateReference;

    @JsonProperty("created_date")
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private final ZonedDateTime createdDate;

    @JsonProperty
    private final ExternalMandateStateWithDetails state;

    @JsonProperty("internal_state")
    private final MandateState internalState;

    public DirectDebitInfoFrontendResponse(Mandate mandate,
                                           String gatewayAccountExternalId,
                                           Payment payment) {
        this.mandateExternalId = mandate.getExternalId().toString();
        this.internalState = mandate.getState();
        this.state = new ExternalMandateStateWithDetails(internalState.toExternal(), mandate.getStateDetails().orElse(null));
        this.gatewayAccountId = mandate.getGatewayAccount().getId();
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.returnUrl = mandate.getReturnUrl();
        this.mandateReference = mandate.getMandateBankStatementReference().orElse(null);
        this.createdDate = mandate.getCreatedDate();
        this.payment = initPayment(payment);
        this.payer = mandate.getPayer().map(this::initPayer).orElse(null);
    }

    private PaymentDetails initPayment(Payment payment) {
        if (payment != null) {
            return new PaymentDetails(
                    payment.getExternalId(),
                    payment.getAmount(),
                    new ExternalPaymentStateWithDetails(payment.getState().toExternal(), payment.getStateDetails().orElse(null)),
                    payment.getDescription(),
                    payment.getReference()
            );
        }
        return null;
    }

    private PayerDetails initPayer(Payer payer) {
        return new PayerDetails(
                payer.getExternalId(),
                payer.getName(),
                payer.getEmail(),
                payer.getAccountRequiresAuthorisation());
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public MandateBankStatementReference getMandateReference() {
        return mandateReference;
    }

    public PaymentDetails getPayment() {
        return payment;
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

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public ExternalMandateStateWithDetails getState() {
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
        if (payment != null ? !payment.equals(that.payment) : that.payment != null) {
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
        if (!createdDate.equals(that.createdDate)) {
            return false;
        }
        if (!internalState.equals(that.internalState)) {
            return false;
        }
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = payer != null ? payer.hashCode() : 0;
        result = 31 * result + (payment != null ? payment.hashCode() : 0);
        result = 31 * result + mandateExternalId.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + mandateReference.hashCode();
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + internalState.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DirectDebitInfoFrontendResponse{" +
                "payerId='" + payer.externalId + "'" +
                ", paymentId='" + payment.externalId + "'" +
                ", mandateId='" + mandateExternalId + "'" +
                ", state='" + state.getMandateState() + "'" +
                ", stateDetails='" + state.getDetails() + "'" +
                ", internalState='" + internalState + "'" +
                ", returnUrl='" + returnUrl + "'" +
                ", mandateReference='" + mandateReference + "'" +
                ", createdDate='" + createdDate + "'" +
                "}";
    }

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public class PaymentDetails {
        @JsonProperty("external_id")
        private String externalId;
        private Long amount;
        private ExternalPaymentStateWithDetails state;
        private String description;
        private String reference;

        public PaymentDetails(String externalId, Long amount,
                              ExternalPaymentStateWithDetails state, String description, String reference) {
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

        public ExternalPaymentStateWithDetails getState() {
            return state;
        }

        public String getDescription() {
            return description;
        }

        public String getReference() {
            return reference;
        }
    }
}
