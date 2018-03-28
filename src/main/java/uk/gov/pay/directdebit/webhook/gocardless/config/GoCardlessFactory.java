package uk.gov.pay.directdebit.webhook.gocardless.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gocardless.GoCardlessClient;
import io.dropwizard.Configuration;
import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.directdebit.webhook.gocardless.support.WebhookVerifier;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import java.net.InetSocketAddress;
import java.net.Proxy;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.constraints.NotNull;

public class GoCardlessFactory extends Configuration {

    @JsonProperty
    private String accessToken;

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

    public GoCardlessClient buildClient(DirectDebitConfig config, SSLSocketFactory sslSocketFactory) {
        GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(accessToken);
        if (isCallingStubs()) {
            builder.withBaseUrl(clientUrl)
                    .withSslSocketFactory(sslSocketFactory);
        } else {
            builder.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyConfig().getHost(), config.getProxyConfig().getPort())));
        }

        return builder.build();
    }

    public WebhookVerifier buildSignatureVerifier() {
        if (StringUtils.isBlank(webhookSecret)) {
            throw new RuntimeException("GoCardless webhook secret is blank");
        }

        return new WebhookVerifier(webhookSecret);
    }

}
