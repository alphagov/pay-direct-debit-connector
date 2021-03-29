package uk.gov.pay.directdebit.filters;

import org.slf4j.MDC;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Optional;

import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;
import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_TYPE;
import static uk.gov.service.payments.logging.LoggingKeys.MANDATE_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PROVIDER;
import static uk.gov.service.payments.logging.LoggingKeys.SECURE_TOKEN;

public class LoggingMDCRequestFilter implements ContainerRequestFilter {

    private final GatewayAccountDao gatewayAccountDao;

    @Inject
    public LoggingMDCRequestFilter(GatewayAccountDao gatewayAccountDao) {
        this.gatewayAccountDao = gatewayAccountDao;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        getPathParameterFromRequest("accountId", requestContext)
                .flatMap(gatewayAccountDao::findByExternalId)
                .ifPresent(gatewayAccount -> {
                    MDC.put(GATEWAY_ACCOUNT_ID, gatewayAccount.getExternalId());
                    MDC.put(PROVIDER, gatewayAccount.getPaymentProvider().name());
                    MDC.put(GATEWAY_ACCOUNT_TYPE, gatewayAccount.getType().name());
                });
        
        getPathParameterFromRequest("mandateExternalId", requestContext)
                .ifPresent(mandateExternalId -> MDC.put(MANDATE_EXTERNAL_ID, mandateExternalId));

        getPathParameterFromRequest("paymentExternalId", requestContext)
                .ifPresent(paymentExternalId -> MDC.put(PAYMENT_EXTERNAL_ID, paymentExternalId));
        
        getPathParameterFromRequest("token", requestContext)
                .ifPresent(token -> MDC.put(SECURE_TOKEN, token));
    }

    private Optional<String> getPathParameterFromRequest(String parameterName, ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getUriInfo().getPathParameters().getFirst(parameterName));
    }
}
