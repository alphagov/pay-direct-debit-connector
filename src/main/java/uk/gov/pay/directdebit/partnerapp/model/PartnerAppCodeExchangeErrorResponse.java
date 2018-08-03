package uk.gov.pay.directdebit.partnerapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectClientResponse;

public class PartnerAppCodeExchangeErrorResponse {
    @JsonProperty("error")
    private String error;
    @JsonProperty("error_description")
    private String errorDescription;

    public static PartnerAppCodeExchangeErrorResponse from(GoCardlessConnectClientResponse clientResponse) {
        PartnerAppCodeExchangeErrorResponse response = new PartnerAppCodeExchangeErrorResponse();
        response.setError(clientResponse.getError());
        response.setErrorDescription(clientResponse.getErrorDescription());

        return response;
    }

    //region <Getters/Setters>
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
    //endregion
}
