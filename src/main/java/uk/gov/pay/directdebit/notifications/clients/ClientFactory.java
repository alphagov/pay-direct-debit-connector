package uk.gov.pay.directdebit.notifications.clients;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.client.ClientProperties;
import uk.gov.pay.commons.utils.logging.LoggingFilter;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

public class ClientFactory {
    
    private final Environment environment;
    private final DirectDebitConfig conf;

    @Inject
    public ClientFactory(Environment environment, DirectDebitConfig conf) {
        this.environment = environment;
        this.conf = conf;
    }

    public Client createWithDropwizardClient(String clientName) {
        return new JerseyClientBuilder(environment)
                .using(conf.getClientConfiguration())
                .withProperty(ClientProperties.READ_TIMEOUT, (int) conf.getCustomJerseyClient().getReadTimeout().toMilliseconds())
                .build(clientName)
                .register(LoggingFilter.class);
    }
}

