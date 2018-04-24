package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import uk.gov.pay.directdebit.webhook.gocardless.config.GoCardlessFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DirectDebitConfig extends Configuration {

    @Valid
    @NotNull
    private GoCardlessFactory goCardless;

    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory;

    @Valid
    @NotNull
    private LinksConfig links;

    @NotNull
    private GraphiteConfig graphiteConfig;

    @NotNull
    private ProxyConfig proxyConfig;


    @NotNull
    private AdminUsersConfig adminUsersConfig;

    @NotNull
    private ExecutorServiceConfig executorServiceConfig;

    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClientConfig;

    @NotNull
    private CustomJerseyClientConfiguration customJerseyClient;

    @JsonProperty("adminusers")
    public AdminUsersConfig getAdminUsersConfig() {
        return adminUsersConfig;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @JsonProperty("goCardless")
    public GoCardlessFactory getGoCardless() {
        return goCardless;
    }

    public LinksConfig getLinks() {
        return links;
    }

    @JsonProperty("executorService")
    public ExecutorServiceConfig getExecutorServiceConfig() {
        return executorServiceConfig;
    }

    @JsonProperty("graphite")
    public GraphiteConfig getGraphiteConfig() {
        return graphiteConfig;
    }

    @JsonProperty("proxy")
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public JerseyClientConfiguration getClientConfiguration() {
        return jerseyClientConfig;
    }

    public CustomJerseyClientConfiguration getCustomJerseyClient() {
        return customJerseyClient;
    }
}
