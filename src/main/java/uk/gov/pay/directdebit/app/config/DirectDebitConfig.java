package uk.gov.pay.directdebit.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//TODO: Awaiting AWS DB environment ready
public class DirectDebitConfig extends Configuration {

    @Valid
    @NotNull
    private GoCardlessFactory goCardless;

//    @Valid
//    @NotNull
//    private DataSourceFactory dataSourceFactory;
//
//    @Valid
//    @NotNull
//    private JPAConfiguration jpaConfiguration;
//

    @JsonProperty("goCardless")
    public GoCardlessFactory getGoCardless() {
        return goCardless;
    }

//    public DataSourceFactory getDataSourceFactory() {
//        return dataSourceFactory;
//    }
//
//    public JPAConfiguration getJpaConfiguration() {
//        return jpaConfiguration;
//    }
}
