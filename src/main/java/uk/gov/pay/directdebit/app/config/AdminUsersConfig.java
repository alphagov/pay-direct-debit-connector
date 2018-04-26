package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class AdminUsersConfig extends Configuration {

    @NotNull
    private String adminUsersUrl;

    public String getAdminUsersUrl() {
        return adminUsersUrl;
    }

}
