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
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.api.PaymentRequestFrontendResponse;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.api.PaymentRequestValidator;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

@Path("/")
public class PaymentRequestResource {
    //has to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{transactionExternalId}";
    public static final String CHARGES_API_PATH = "/v1/api/accounts/{accountId}/charges";


    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestResource.class);
    private final TransactionService transactionService;
    private final MandateService mandateService;
    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();

    @Inject
    public PaymentRequestResource(TransactionService transactionService,
            MandateService mandateService) {
        this.transactionService = transactionService;
        this.mandateService = mandateService;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getCharge(@PathParam("accountId") String accountExternalId, @PathParam("transactionExternalId") String transactionExternalId, @Context UriInfo uriInfo) {
        PaymentRequestResponse response = transactionService.getPaymentWithExternalId(accountExternalId, transactionExternalId, uriInfo);
        return Response.ok(response).build();
    }

    @GET
    @Path("/v1/accounts/{accountId}/mandates/{mandateExternalId}/payments/{transactionExternalId}")
    @Produces(APPLICATION_JSON)
    public Response getMandateWithTransaction(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId, @PathParam("transactionExternalId") String transactionExternalId) {
        LOGGER.info("Retrieving mandate {} and charge {} for frontend", mandateExternalId, transactionExternalId);
        Mandate mandate = mandateService.findByExternalId(mandateExternalId);
        Transaction transaction = transactionService.findTransactionForExternalIdAndGatewayAccountExternalId(transactionExternalId, accountExternalId);
        PaymentRequestFrontendResponse response = transactionService.populateFrontendResponse(accountExternalId, mandate, transaction);
        return Response.ok(response).build();
    }

    @GET
    @Path("/v1/accounts/{accountId}/mandates/{mandateExternalId}")
    @Produces(APPLICATION_JSON)
    public Response getMandateWithoutTransaction(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId) {
        LOGGER.info("Retrieving mandate {} for frontend", mandateExternalId);
        Mandate mandate = mandateService.findByExternalId(mandateExternalId);
        PaymentRequestFrontendResponse response = transactionService.populateFrontendResponse(accountExternalId, mandate, null);
        return Response.ok(response).build();
    }
    
    @POST
    @Path(CHARGES_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response createNewPaymentRequest(@PathParam("accountId") String accountExternalId, Map<String, String> paymentRequest, @Context UriInfo uriInfo) {
        LOGGER.info("Received new one-off payment request");
        paymentRequestValidator.validate(paymentRequest);
        paymentRequest.put("agreement_type", "ONE_OFF");
        Mandate mandate = mandateService
                .createMandate(paymentRequest, accountExternalId);
        PaymentRequestResponse response = transactionService.createTransaction(paymentRequest, mandate, accountExternalId, uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}
