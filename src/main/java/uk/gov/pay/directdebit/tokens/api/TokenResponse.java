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

    private TokenResponse(String paymentExternalId, String returnUrl, Long amount, String type, String state) {
        this.paymentExternalId = paymentExternalId;
        this.amount = amount;
        this.type = type;
        this.state = state;
        this.returnUrl = returnUrl;
    }

    public static TokenResponse from(Transaction transaction) {
        return new TokenResponse(
                transaction.getPaymentRequestExternalId(),
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
        if (!amount.equals(that.amount)) return false;
        if (!type.equals(that.type)) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = paymentExternalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "externalId=" + paymentExternalId +
                ", return_url=" + returnUrl +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
