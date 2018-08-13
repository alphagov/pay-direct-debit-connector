package uk.gov.pay.directdebit.partnerapp.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

public class GoCardlessAppConnectAccountEntity {
    private Long id;
    private String token = RandomIdGenerator.newId();
    private Long gatewayAccountId;
    private Boolean active;
    private String redirectUri;

    //region <Getters/Setters>

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public void setGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    //endregion
}
