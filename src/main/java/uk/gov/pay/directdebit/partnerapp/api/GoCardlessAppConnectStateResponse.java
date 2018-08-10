package uk.gov.pay.directdebit.partnerapp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectTokenEntity;

public class GoCardlessAppConnectStateResponse {

    @JsonProperty("token")
    private String token;
    @JsonProperty("active")
    private Boolean active;

    public static GoCardlessAppConnectStateResponse from(GoCardlessAppConnectTokenEntity entity) {
        GoCardlessAppConnectStateResponse response = new GoCardlessAppConnectStateResponse();
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
