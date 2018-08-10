package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GoCardlessAppConnectConfig extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectUrlTest;

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectUrlLive;

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectClientSecretTest;

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectClientSecretLive;

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectClientIdTest;

    @Valid
    @NotNull
    @JsonProperty
    private String goCardlessConnectClientIdLive;

    @NotNull
    public String getGoCardlessConnectUrlTest() {
        return goCardlessConnectUrlTest;
    }

    @NotNull
    public String getGoCardlessConnectUrlLive() {
        return goCardlessConnectUrlLive;
    }

    @NotNull
    public String getGoCardlessConnectClientSecretTest() {
        return goCardlessConnectClientSecretTest;
    }

    @NotNull
    public String getGoCardlessConnectClientSecretLive() {
        return goCardlessConnectClientSecretLive;
    }

    @NotNull
    public String getGoCardlessConnectClientIdTest() {
        return goCardlessConnectClientIdTest;
    }

    @NotNull
    public String getGoCardlessConnectClientIdLive() {
        return goCardlessConnectClientIdLive;
    }
}
