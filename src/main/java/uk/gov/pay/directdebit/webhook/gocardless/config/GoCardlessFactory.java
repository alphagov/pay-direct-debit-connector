package uk.gov.pay.directdebit.webhook.gocardless.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gocardless.GoCardlessClient;
import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;

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

    public WebhookVerifier buildSignatureVerifier() {
        if (StringUtils.isBlank(webhookSecret)) {
            return null;
            // do not throw exception for now (until we have GoCardless webhook secret for testing)
            //throw new RuntimeException("GoCardless webhook secret is blank");
        }

        return new WebhookVerifier(webhookSecret);
    }

}
