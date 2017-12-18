package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class ProxyConfig extends Configuration {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
