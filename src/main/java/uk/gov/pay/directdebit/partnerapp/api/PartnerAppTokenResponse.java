package uk.gov.pay.directdebit.partnerapp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;

public class PartnerAppTokenResponse {

    @JsonProperty("token")
    private String token;
    @JsonProperty("active")
    private Boolean active;

    public static PartnerAppTokenResponse from(PartnerAppTokenEntity entity) {
        PartnerAppTokenResponse response = new PartnerAppTokenResponse();
        response.setToken(entity.getToken());
        response.setActive(entity.isActive());
        return response;
    }

    //region <Getters/Setters>

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    //endregion
}
