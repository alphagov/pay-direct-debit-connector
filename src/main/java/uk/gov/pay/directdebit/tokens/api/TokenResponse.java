package uk.gov.pay.directdebit.tokens.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class TokenResponse {

    @JsonProperty("external_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private MandateExternalId mandateExternalId;

    @JsonProperty("state")
    private String state;

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

    @JsonProperty("transaction_external_id")
    private String transactionExternalId;

    private TokenResponse(MandateExternalId mandateExternalId,
                          Long gatewayAccountId,
                          String gatewayAccountExternalId,
                          String mandateReference,
                          String returnUrl,
                          String mandateType,
                          String state,
                          String transactionExternalId) {
        this.mandateExternalId = mandateExternalId;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.mandateType = mandateType;
        this.state = state;
        this.returnUrl = returnUrl;
        this.mandateReference = mandateReference;
        this.transactionExternalId = transactionExternalId;
    }

    public static TokenResponse from(Mandate mandate, String transactionExternalId) {
        return new TokenResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                mandate.getGatewayAccount().getExternalId(),
                mandate.getMandateReference(),
                mandate.getReturnUrl(),
                mandate.getType().toString(),
                mandate.getState().toString(),
                transactionExternalId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TokenResponse that = (TokenResponse) o;

        if (!mandateExternalId.equals(that.mandateExternalId)) {
            return false;
        }
        if (!state.equals(that.state)) {
            return false;
        }
        if (!returnUrl.equals(that.returnUrl)) {
            return false;
        }
        if (!gatewayAccountId.equals(that.gatewayAccountId)) {
            return false;
        }
        if (!gatewayAccountExternalId.equals(that.gatewayAccountExternalId)) {
            return false;
        }
        if (mandateReference != null ? !mandateReference.equals(that.mandateReference) : that.mandateReference != null) {
            return false;
        }
        if (!mandateType.equals(that.mandateType)) {
            return false;
        }
        return transactionExternalId != null ? transactionExternalId
                .equals(that.transactionExternalId) : that.transactionExternalId == null;
    }

    @Override
    public int hashCode() {
        int result = mandateExternalId.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + (mandateReference != null ? mandateReference.hashCode() : 0);
        result = 31 * result + mandateType.hashCode();
        result = 31 * result + (transactionExternalId != null ? transactionExternalId.hashCode()
                : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "external_id=" + mandateExternalId +
                ", transaction_external_id=" + transactionExternalId +
                ", gateway_account_id=" + gatewayAccountId +
                ", gateway_account_external_id=" + gatewayAccountExternalId +
                ", mandateReference=" + mandateReference +
                ", mandateType=" + mandateType +
                ", state=" + state +
                ", return_url=" + returnUrl +
                '}';
    }

}
