package uk.gov.pay.directdebit.webhook.gocardless.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gocardless.GoCardlessClient;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class GoCardlessFactory extends Configuration {
    
    @JsonProperty
    private String webhookSecret;

    // if specified, we are going to create a gocardless client which connects to that url. This can be used to send requests to our stubs instead of gocardless. Otherwise, we will hit the real gocardless and the url will depend on the environment (SANDBOX/LIVE)
    @JsonProperty
    private String clientUrl;

    @JsonProperty
    @NotNull
    private GoCardlessClient.Environment environment;

    public Boolean isCallingStubs() {
        return clientUrl != null;
    }
    
    public String getWebhookSecret() {
        return webhookSecret;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public GoCardlessClient.Environment getEnvironment() {
        return environment;
    }

}
