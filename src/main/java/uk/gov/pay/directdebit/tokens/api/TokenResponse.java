package uk.gov.pay.directdebit.tokens.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.Transaction;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class TokenResponse {

    @JsonProperty("external_id")
    private String paymentExternalId;

    @JsonProperty("amount")
    private Long amount;

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

    @JsonProperty("description")
    private String description;


    private TokenResponse(String paymentExternalId,
                          Long gatewayAccountId,
                          String gatewayAccountExternalId,
                          String description,
                          String returnUrl,
                          Long amount,
                          String type,
                          String state) {
        this.paymentExternalId = paymentExternalId;
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.amount = amount;
        this.type = type;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
    }

    public static TokenResponse from(Transaction transaction) {
        return new TokenResponse(
                transaction.getPaymentRequest().getExternalId(),
                transaction.getGatewayAccountId(),
                transaction.getGatewayAccountExternalId(),
                transaction.getPaymentRequestDescription(),
                transaction.getPaymentRequestReturnUrl(),
                transaction.getAmount(),
                transaction.getType().toString(),
                transaction.getState().toString()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenResponse that = (TokenResponse) o;
        if (!returnUrl.equals(that.returnUrl)) return false;
        if (!paymentExternalId.equals(that.paymentExternalId)) return false;
        if (!gatewayAccountId.equals(that.gatewayAccountId)) return false;
        if (!gatewayAccountExternalId.equals(that.gatewayAccountExternalId)) return false;
        if (!description.equals(that.description)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!type.equals(that.type)) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = paymentExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "externalId=" + paymentExternalId +
                ", description=" + description +
                ", gateway_account_id=" + gatewayAccountId +
                ", gateway_account_external_id=" + gatewayAccountExternalId +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                ", return_url=" + returnUrl +
                '}';
    }

}
