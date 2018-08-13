package uk.gov.pay.directdebit.partnerapp.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;

public class GoCardlessAppConnectStateResponse {

    @JsonProperty("state")
    private String state;
    @JsonProperty("active")
    private Boolean active;

    public static GoCardlessAppConnectStateResponse from(GoCardlessAppConnectAccountEntity entity) {
        GoCardlessAppConnectStateResponse response = new GoCardlessAppConnectStateResponse();
        response.setState(entity.getToken());
        response.setActive(entity.isActive());
        return response;
    }

    //region <Getters/Setters>

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    //endregion
}
