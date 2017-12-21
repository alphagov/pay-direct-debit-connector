package uk.gov.pay.directdebit.payers.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.services.PayerService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PayerResource {
    private static final String PAYERS_API_PATH = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers";
    private static final String PAYER_API_PATH = PAYERS_API_PATH + "/{payerExternalId}";

    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerResource.class);
    private final PayerService payerService;
    private final CreatePayerValidator createPayerValidator = new CreatePayerValidator();

    public PayerResource(PayerService payerService) {
        this.payerService = payerService;
    }

    @POST
    @Path(PAYERS_API_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPayer(@PathParam("accountId") Long accountId, @PathParam("paymentRequestExternalId") String paymentRequestExternalId, Map<String, String> createPayerRequest, @Context UriInfo uriInfo) {
        createPayerValidator.validate(createPayerRequest);
        LOGGER.info("Create new payer request received for payment request {} ", paymentRequestExternalId);
        CreatePayerResponse createPayerResponse = CreatePayerResponse.from(payerService.create(paymentRequestExternalId, createPayerRequest));
        URI newPayerLocation = URIBuilder.selfUriFor(uriInfo, PAYER_API_PATH, String.valueOf(accountId), paymentRequestExternalId, createPayerResponse.getPayerExternalId());
        return Response.created(newPayerLocation).entity(createPayerResponse).build();
    }

}
