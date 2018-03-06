package uk.gov.pay.directdebit.mandate.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.noContent;

@Path("/")
public class ConfirmPaymentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmPaymentResource.class);
    private final PaymentProviderFactory paymentProviderFactory;

    @Inject
    public ConfirmPaymentResource(PaymentProviderFactory paymentProviderFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/confirm")
    public Response confirm(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("paymentRequestExternalId") String paymentRequestExternalId) {
        LOGGER.info("Confirming payment for payment request with id: {}", paymentRequestExternalId);

        DirectDebitPaymentProvider service = paymentProviderFactory.getServiceFor(gatewayAccount.getPaymentProvider());
        service.confirm(paymentRequestExternalId, gatewayAccount);
        LOGGER.info("Confirmed payment for payment request with id: {}", paymentRequestExternalId);

        return noContent().build();
    }
}
