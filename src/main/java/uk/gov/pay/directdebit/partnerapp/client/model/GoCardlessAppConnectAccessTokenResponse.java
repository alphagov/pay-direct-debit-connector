package uk.gov.pay.directdebit.partnerapp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoCardlessAppConnectAccessTokenResponse {
    @JsonProperty("access_token")
    private PaymentProviderAccessToken accessToken;
    @JsonProperty("organisation_id")
    private GoCardlessOrganisationId organisationId;
    @JsonProperty("error")
    private String error;
    @JsonProperty("error_description")
    private String errorDescription;

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

    public PaymentProviderAccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(PaymentProviderAccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public GoCardlessOrganisationId getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(GoCardlessOrganisationId organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public String toString() {
        return "GoCardlessConnectClientResponse{" +
                ", organisationId=" + organisationId +
                ", error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }

    //endregion

}
