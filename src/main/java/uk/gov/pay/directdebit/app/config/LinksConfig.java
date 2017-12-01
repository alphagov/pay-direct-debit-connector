package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

public class LinksConfig extends Configuration {

    private String frontendUrl;

    public String getFrontendUrl() {
        return frontendUrl;
    }
}
