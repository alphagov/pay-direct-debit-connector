package uk.gov.pay.directdebit.app.config;

import io.dropwizard.Configuration;

public class EncryptionConfig extends Configuration {

    private String encryptDBSalt;

    public String getEncryptDBSalt() {
        return encryptDBSalt;
    }

}
