package uk.gov.pay.directdebit.mandate.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.Response.noContent;

@Path("/")
public class ConfirmPaymentResource {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmPaymentResource.class);
    private final PaymentConfirmService paymentConfirmService;

    public ConfirmPaymentResource(PaymentConfirmService paymentConfirmService) {
        this.paymentConfirmService = paymentConfirmService;
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/payment-requests/{paymentRequestId}/confirm")
    public Response confirm(@PathParam("accountId") Long accountId, @PathParam("paymentRequestId") String paymentRequestId, @Context UriInfo uriInfo) {
        logger.info("Confirming payment - {}", paymentRequestId);
        paymentConfirmService.confirm(accountId, paymentRequestId);
        return noContent().build();
    }
}
