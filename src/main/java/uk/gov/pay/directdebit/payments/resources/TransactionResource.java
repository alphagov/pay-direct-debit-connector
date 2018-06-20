package uk.gov.pay.directdebit.payments.resources;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.api.TransactionRequestValidator;
import uk.gov.pay.directdebit.payments.api.TransactionResponse;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

@Path("/")
public class TransactionResource {
    //has to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{transactionExternalId}";
    public static final String CHARGES_API_PATH = "/v1/api/accounts/{accountId}/charges";


    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);
    private final TransactionService transactionService;
    private final MandateService mandateService;
    private final TransactionRequestValidator transactionRequestValidator = new TransactionRequestValidator();

    @Inject
    public TransactionResource(TransactionService transactionService,
            MandateService mandateService) {
        this.transactionService = transactionService;
        this.mandateService = mandateService;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam("accountId") String accountExternalId, @PathParam("transactionExternalId") String transactionExternalId, @Context UriInfo uriInfo) {
        TransactionResponse response = transactionService.getPaymentWithExternalId(accountExternalId, transactionExternalId, uriInfo);
        return Response.ok(response).build();
    }
    
    @POST
    @Path(CHARGES_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response createOneOffPayment(@PathParam("accountId") String accountExternalId, Map<String, String> transactionRequest, @Context UriInfo uriInfo) {
        LOGGER.info("Received new one-off payment request");
        transactionRequestValidator.validate(transactionRequest);
        transactionRequest.put("agreement_type", MandateType.ONE_OFF.toString());
        Mandate mandate = mandateService
                .createMandate(transactionRequest, accountExternalId);
        TransactionResponse response = transactionService.createTransaction(transactionRequest, mandate, accountExternalId, uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}
