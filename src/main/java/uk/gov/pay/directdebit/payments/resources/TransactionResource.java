package uk.gov.pay.directdebit.payments.resources;

import com.codahale.metrics.annotation.Timed;
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
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequestValidator;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.CreatePaymentRequest;
import uk.gov.pay.directdebit.payments.api.TransactionRequestValidator;
import uk.gov.pay.directdebit.payments.api.TransactionResponse;
import uk.gov.pay.directdebit.payments.model.Transaction;
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
    private final MandateServiceFactory mandateServiceFactory;

    private final TransactionRequestValidator transactionRequestValidator = new TransactionRequestValidator();
    private final CollectPaymentRequestValidator collectPaymentRequestValidator = new CollectPaymentRequestValidator();

    @Inject
    public TransactionResource(TransactionService transactionService,
            MandateServiceFactory mandateServiceFactory) {
        this.transactionService = transactionService;
        this.mandateServiceFactory = mandateServiceFactory;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getCharge(@PathParam("accountId") String accountExternalId, @PathParam("transactionExternalId") String transactionExternalId, @Context UriInfo uriInfo) {
        TransactionResponse response = transactionService.getPaymentWithExternalId(accountExternalId, transactionExternalId, uriInfo);
        return Response.ok(response).build();
    }

    @POST
    @Path(CHARGES_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createOneOffPayment(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> transactionRequestMap, @Context UriInfo uriInfo) {
        LOGGER.info("Received new one-off payment request");
        transactionRequestValidator.validate(transactionRequestMap);
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.of(transactionRequestMap);
        Transaction transaction = mandateServiceFactory.getOneOffMandateService().create(gatewayAccount, createPaymentRequest);
        TransactionResponse response = transactionService.createPaymentResponseWithAllLinks(transaction, gatewayAccount.getExternalId(), uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/charges/collect")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response collectPaymentFromMandate(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequestMap, @Context UriInfo uriInfo) {
        LOGGER.info("Received collect payment from mandate request");
        collectPaymentRequestValidator.validate(collectPaymentRequestMap);
        CollectPaymentRequest collectPaymentRequest = CollectPaymentRequest.of(collectPaymentRequestMap);
        Mandate mandate = mandateServiceFactory
                .getMandateQueryService()
                .findByExternalId(collectPaymentRequest.getMandateExternalId());
        Transaction paymentToCollect = transactionService.createOnDemandTransaction(gatewayAccount, mandate, collectPaymentRequest);
        CollectPaymentResponse response = transactionService.collectPaymentResponseWithSelfLink(paymentToCollect, gatewayAccount.getExternalId(), uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}
