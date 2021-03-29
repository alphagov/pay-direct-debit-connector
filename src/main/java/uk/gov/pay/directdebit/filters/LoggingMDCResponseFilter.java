package uk.gov.pay.directdebit.filters;

import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.List;

import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;
import static uk.gov.service.payments.logging.LoggingKeys.GATEWAY_ACCOUNT_TYPE;
import static uk.gov.service.payments.logging.LoggingKeys.MANDATE_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;
import static uk.gov.service.payments.logging.LoggingKeys.PROVIDER;
import static uk.gov.service.payments.logging.LoggingKeys.SECURE_TOKEN;

public class LoggingMDCResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        List.of(GATEWAY_ACCOUNT_ID, PROVIDER, GATEWAY_ACCOUNT_TYPE, MANDATE_EXTERNAL_ID, PAYMENT_EXTERNAL_ID, SECURE_TOKEN)
                .forEach(MDC::remove);
    }
}
