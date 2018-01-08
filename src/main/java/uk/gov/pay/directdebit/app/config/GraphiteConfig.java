package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

public class GraphiteConfig extends Configuration {

    private String host;
    private Integer port;

    private static final String SERVICE_METRICS_NODE = "dd-connector";
    private static final int GRAPHITE_SENDING_PERIOD_SECONDS = 10;

    public String getNode() {
        return SERVICE_METRICS_NODE;
    }

    public int getSendingPeriod() {
        return GRAPHITE_SENDING_PERIOD_SECONDS;
    }
    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
