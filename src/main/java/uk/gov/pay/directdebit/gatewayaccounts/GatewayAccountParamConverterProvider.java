package uk.gov.pay.directdebit.gatewayaccounts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

@Provider
public class GatewayAccountParamConverterProvider implements ParamConverterProvider {

    private GatewayAccountDao gatewayAccountDao;

    @Inject
    public GatewayAccountParamConverterProvider(GatewayAccountDao gatewayAccountDao) {
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public GatewayAccountConverter createGatewayAccountConverter() {
        return new GatewayAccountConverter();
    }
    public class GatewayAccountConverter implements ParamConverter<GatewayAccount> {

        @Override
        public GatewayAccount fromString(String externalAccountId) {
            return gatewayAccountDao
                    .findByExternalId(externalAccountId)
                    .orElseThrow(() -> new GatewayAccountNotFoundException(externalAccountId));
        }

        @Override
        public String toString(GatewayAccount gatewayAccount) {
            return gatewayAccount.getId().toString();
        }
    }
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if(rawType.equals(GatewayAccount.class)){
            return ((ParamConverter<T>) createGatewayAccountConverter());
        }
        return null;
    }
}
