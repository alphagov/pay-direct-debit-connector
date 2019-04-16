package uk.gov.pay.directdebit.gatewayaccounts;

import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import javax.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

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
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(GatewayAccount.class)) {
            return (ParamConverter<T>) createGatewayAccountConverter();
        }
        return null;
    }

}
