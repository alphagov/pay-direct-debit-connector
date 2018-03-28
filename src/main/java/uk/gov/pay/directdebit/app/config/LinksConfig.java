package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.Configuration;

public class LinksConfig extends Configuration {

    private String frontendUrl;

    public String getFrontendUrl() {
        return frontendUrl;
    }

    @JsonIgnore
    public String getDirectDebitGuaranteeUrl() {
        return getFrontendUrl() + "/direct-debit-guarantee";
    }
}
