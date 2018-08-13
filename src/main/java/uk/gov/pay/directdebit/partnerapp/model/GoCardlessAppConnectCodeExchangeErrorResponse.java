package uk.gov.pay.directdebit.partnerapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessAppConnectAccessTokenResponse;

public class GoCardlessAppConnectCodeExchangeErrorResponse {
    @JsonProperty("error")
    private String error;
    @JsonProperty("message")
    private String message;

    public static GoCardlessAppConnectCodeExchangeErrorResponse from(GoCardlessAppConnectAccessTokenResponse clientResponse) {
        GoCardlessAppConnectCodeExchangeErrorResponse response = new GoCardlessAppConnectCodeExchangeErrorResponse();
        response.setError(clientResponse.getError());
        response.setMessage(clientResponse.getErrorDescription());

        return response;
    }

    //region <Getters/Setters>
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "GoCardlessAppConnectCodeExchangeErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    //endregion
}
