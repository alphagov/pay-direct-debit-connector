package uk.gov.pay.directdebit.payers.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.api.PaymentRequestValidator;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.ok;
import static uk.gov.pay.directdebit.common.resources.V1ApiPaths.ROOT_PATH;

@Path("/")
public class PayerResource {
    private static final Logger logger = PayLoggerFactory.getLogger(PayerResource.class);
    private final PayerService payerService;
    private final CreatePayerValidator createPayerValidator = new CreatePayerValidator();

    public PayerResource(PayerService payerService) {
        this.payerService = payerService;
    }

    @POST
    @Path("/payment_requests/{paymentRequestId}/payers")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPayer(@PathParam("paymentRequestExternalId") String paymentRequestExternalId, Map<String, String> createPayerRequest) {
        createPayerValidator.validate(createPayerRequest);
        //fixme do not log whole body, it has PII
        logger.info("Create new payer request received: body - {}", createPayerRequest.toString());
        CreatePayerResponse createPayerResponse = payerService.create(paymentRequestExternalId, createPayerRequest);
        //add event
        //validate sort code and account length
        //create payer
        //update charge status
        //add event
        //returns details to frontend
        return ok().entity(createPayerResponse).build();
    }

}
