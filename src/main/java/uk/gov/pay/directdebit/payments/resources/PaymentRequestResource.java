package uk.gov.pay.directdebit.payments.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.api.PaymentRequestValidator;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

@Path("/")
public class PaymentRequestResource {
    //have to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{paymentRequestExternalId}";
    public static final String CHARGES_API_PATH = "/v1/api/accounts/{accountId}/charges";

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestResource.class);
    private final PaymentRequestService paymentRequestService;
    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();

    @Inject
    public PaymentRequestResource(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }


    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam("accountId") String accountExternalId, @PathParam("paymentRequestExternalId") String paymentRequestExternalId, @Context UriInfo uriInfo) {
        PaymentRequestResponse response = paymentRequestService.getPaymentWithExternalId(accountExternalId, paymentRequestExternalId, uriInfo);
        return Response.ok(response).build();
    }

    @POST
    @Path(CHARGES_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response createNewPaymentRequest(@PathParam("accountId") String accountExternalId, Map<String, String> paymentRequest, @Context UriInfo uriInfo) {
        paymentRequestValidator.validate(paymentRequest);
        LOGGER.info("Creating new payment request - {}", paymentRequest.toString());
        PaymentRequestResponse response = paymentRequestService.createCharge(paymentRequest, accountExternalId, uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}
