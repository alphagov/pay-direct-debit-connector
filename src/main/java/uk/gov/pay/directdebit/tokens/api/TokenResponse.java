package uk.gov.pay.directdebit.tokens.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.mandate.model.Mandate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class TokenResponse {

    @JsonProperty("external_id")
    private String mandateExternalId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("state")
    private String state;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;

    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("transaction_external_id")
    private String transactionExternalId;


    private TokenResponse(String paymentExternalId,
            Long gatewayAccountId,
            String gatewayAccountExternalId,
            String reference,
            String returnUrl,
            String type,
            String state,
            String transactionExternalId) {
        this.mandateExternalId = paymentExternalId;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.type = type;
        this.state = state;
        this.returnUrl = returnUrl;
        this.reference = reference;
        this.transactionExternalId = transactionExternalId;
    }

    public static TokenResponse from(Mandate mandate, String transactionExternalId) {
        return new TokenResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                mandate.getGatewayAccount().getExternalId(),
                mandate.getReference(),
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
        if (!type.equals(that.type)) {
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
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) {
            return false;
        }
        return transactionExternalId != null ? transactionExternalId
                .equals(that.transactionExternalId) : that.transactionExternalId == null;
    }

    @Override
    public int hashCode() {
        int result = mandateExternalId.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + (transactionExternalId != null ? transactionExternalId.hashCode()
                : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "external_id=" + mandateExternalId +
                ", reference=" + reference +
                ", transaction_external_id=" + transactionExternalId +
                ", gateway_account_id=" + gatewayAccountId +
                ", gateway_account_external_id=" + gatewayAccountExternalId +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", return_url=" + returnUrl +
                '}';
    }

}
