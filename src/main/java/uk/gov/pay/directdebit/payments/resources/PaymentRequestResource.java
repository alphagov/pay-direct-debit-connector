package uk.gov.pay.directdebit.payments.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.api.PaymentRequestValidator;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;
import static uk.gov.pay.directdebit.common.resources.V1ApiPaths.CHARGES_API_PATH;
import static uk.gov.pay.directdebit.common.resources.V1ApiPaths.CHARGE_API_PATH;
import static uk.gov.pay.directdebit.common.util.ResponseUtil.*;

@Path("/")
public class PaymentRequestResource {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestResource.class);
    private final PaymentRequestService paymentRequestService;
    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();
    private final String ACCOUNT_ID = "accountId";

    public PaymentRequestResource(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam(ACCOUNT_ID) Long accountId, @PathParam("paymentRequestExternalId") String paymentRequestExternalId, @Context UriInfo uriInfo) {
        return paymentRequestService.getPaymentWithExternalId(paymentRequestExternalId, uriInfo)
                .map(chargeResponse -> Response.ok(chargeResponse).build())
                .orElseGet(() -> responseWithChargeNotFound(paymentRequestExternalId));
    }

    @POST
    @Path(CHARGES_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response createNewPaymentRequest(@PathParam(ACCOUNT_ID) Long accountId, Map<String, String> paymentRequest, @Context UriInfo uriInfo) {
        //this is a good case for an Either :P
        return paymentRequestValidator.validate(paymentRequest).map(errors -> {
                    switch(errors.getType()) {
                        case MISSING_MANDATORY_FIELDS:
                            return fieldsMissingResponse(errors.getFields());
                        case INVALID_SIZE_FIELDS:
                            return fieldsInvalidSizeResponse(errors.getFields());
                        default:
                            return fieldsInvalidResponse(errors.getFields());
                    }
                }
        ).orElseGet(() -> {
            logger.info("Creating new payment request - {}", paymentRequest.toString());
            return paymentRequestService.create(paymentRequest, accountId, uriInfo)
                    .map(response -> created(response.getLink("self")).entity(response).build())
                    .orElseGet(() -> notFoundResponse("Unknown gateway account: " + accountId));
        });
    }
}
