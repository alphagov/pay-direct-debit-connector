package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gocardless.GoCardlessClient;

import javax.validation.constraints.NotNull;

public class GoCardlessFactory {

    @JsonProperty
    private String accessToken;

    @JsonProperty
    private String webhookSecret;

    @JsonProperty
    @NotNull
    private GoCardlessClient.Environment environment;

    public GoCardlessClient buildClient() {
        return GoCardlessClient.create(accessToken, environment);
    }

}
