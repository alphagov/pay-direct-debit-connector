package uk.gov.pay.directdebit.tokens.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private MandateBankStatementReference mandateReference;

    private TokenResponse(MandateExternalId mandateExternalId,
                          Long gatewayAccountId,
                          String gatewayAccountExternalId,
                          MandateBankStatementReference mandateReference,
                          String returnUrl,
                          String state) {
        this.mandateExternalId = mandateExternalId;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.state = state;
        this.returnUrl = returnUrl;
        this.mandateReference = mandateReference;
    }

    public static TokenResponse from(Mandate mandate) {
        return new TokenResponse(
                mandate.getExternalId(),
                mandate.getGatewayAccount().getId(),
                mandate.getGatewayAccount().getExternalId(),
                mandate.getMandateBankStatementReference().orElse(null),
                mandate.getReturnUrl(),
                mandate.getState().toString()
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
        } else return true;
    }

    @Override
    public int hashCode() {
        int result = mandateExternalId.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + mandateReference.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "external_id=" + mandateExternalId +
                ", gateway_account_id=" + gatewayAccountId +
                ", gateway_account_external_id=" + gatewayAccountExternalId +
                ", mandateReference=" + mandateReference +
                ", state=" + state +
                ", return_url=" + returnUrl +
                '}';
    }

}
