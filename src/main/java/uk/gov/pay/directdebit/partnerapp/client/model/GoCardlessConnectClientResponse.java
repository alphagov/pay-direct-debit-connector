package uk.gov.pay.directdebit.partnerapp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoCardlessConnectClientResponse {
    @JsonProperty("access_token")
    private PaymentProviderAccessToken accessToken;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("organisation_id")
    private PaymentProviderOrganisationIdentifier organisationId;
    @JsonProperty("email")
    private String email;
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

    public PaymentProviderOrganisationIdentifier getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(PaymentProviderOrganisationIdentifier organisationId) {
        this.organisationId = organisationId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    //endregion


    @Override
    public String toString() {
        return "GoCardlessConnectClientResponse{" +
                "accessToken=" + accessToken +
                ", scope='" + scope + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", organisationId=" + organisationId +
                ", email='" + email + '\'' +
                ", error='" + error + '\'' +
                ", errorDescription='" + errorDescription + '\'' +
                '}';
    }
}
