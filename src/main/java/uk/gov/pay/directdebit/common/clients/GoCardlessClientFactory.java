package uk.gov.pay.directdebit.common.clients;

import com.gocardless.GoCardlessClient;
import com.google.common.collect.Maps;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.webhook.gocardless.config.GoCardlessFactory;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Optional;

public class GoCardlessClientFactory {

    private final Map<PaymentProviderAccessToken, GoCardlessClientFacade> clients;
    private final SSLSocketFactory sslSocketFactory;
    private final DirectDebitConfig configuration;

    public GoCardlessClientFactory(
            DirectDebitConfig configuration,
            SSLSocketFactory sslSocketFactory) {
        this.configuration = configuration;
        this.sslSocketFactory = sslSocketFactory;
        this.clients = Maps.newConcurrentMap();
    }

    public GoCardlessClientFacade getClientFor(Optional<PaymentProviderAccessToken> maybeAccessToken) {
        //backward compatibility for now, will use the token in the config if it's not there
        PaymentProviderAccessToken accessToken = maybeAccessToken.orElse(PaymentProviderAccessToken.of(configuration.getGoCardless().getAccessToken()));
        return clients.computeIfAbsent(accessToken, token -> {
            GoCardlessClientWrapper clientWrapper = new GoCardlessClientWrapper(createGoCardlessClient(token));
            return new GoCardlessClientFacade(clientWrapper);
        });
    }

    private GoCardlessClient createGoCardlessClient(PaymentProviderAccessToken accessToken) {
        GoCardlessFactory goCardlessFactory = configuration.getGoCardless();
        GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(accessToken.toString());

        if (goCardlessFactory.isCallingStubs()) {
            return builder.withBaseUrl(goCardlessFactory.getClientUrl())
                    .withSslSocketFactory(sslSocketFactory)
                    .build();
        }
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(configuration.getProxyConfig().getHost(), configuration.getProxyConfig().getPort()));
        return builder.withEnvironment(goCardlessFactory.getEnvironment())
                .withProxy(proxy).build();
    }
}
