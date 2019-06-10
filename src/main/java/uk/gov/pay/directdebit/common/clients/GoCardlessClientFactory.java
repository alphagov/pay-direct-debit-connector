package uk.gov.pay.directdebit.common.clients;

import com.gocardless.GoCardlessClient;
import com.google.common.collect.Maps;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.common.exception.NoAccessTokenException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.webhook.gocardless.config.GoCardlessFactory;

import java.util.Map;
import java.util.Optional;

public class GoCardlessClientFactory {

    private final Map<PaymentProviderAccessToken, GoCardlessClientFacade> clients;
    private final DirectDebitConfig configuration;

    public GoCardlessClientFactory(DirectDebitConfig configuration) {
        this.configuration = configuration;
        this.clients = Maps.newConcurrentMap();
    }

    public GoCardlessClientFacade getClientFor(Optional<PaymentProviderAccessToken> maybeAccessToken) {
        //backward compatibility for now, will use the token in the config if it's not there
        PaymentProviderAccessToken accessToken = maybeAccessToken
                .orElseThrow(() -> new NoAccessTokenException("No access token"));
        return clients.computeIfAbsent(accessToken, token -> {
            GoCardlessClientWrapper clientWrapper = new GoCardlessClientWrapper(createGoCardlessClient(token));
            return new GoCardlessClientFacade(clientWrapper);
        });
    }

    private GoCardlessClient createGoCardlessClient(PaymentProviderAccessToken accessToken) {
        GoCardlessFactory goCardlessFactory = configuration.getGoCardless();
        GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(accessToken.toString());

        if (goCardlessFactory.isCallingStubs()) {
            return builder
                    .withBaseUrl(goCardlessFactory.getClientUrl())
                    .build();
        }

        return builder
                .withEnvironment(goCardlessFactory.getEnvironment())
                .build();
    }
}
