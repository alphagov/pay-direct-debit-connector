package uk.gov.pay.directdebit.payments.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequestValidator;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.TransactionRequestValidator;
import uk.gov.pay.directdebit.payments.api.TransactionResponse;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

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
public class TransactionResource {
    //has to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{transactionExternalId}";
    public static final String CHARGES_API_PATH = "/v1/api/accounts/{accountId}/charges";


    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);
    private final TransactionService transactionService;
    private final MandateService mandateService;
    private final TransactionRequestValidator transactionRequestValidator = new TransactionRequestValidator();
    private final CollectPaymentRequestValidator collectPaymentRequestValidator = new CollectPaymentRequestValidator();
    private final PaymentProviderFactory paymentProviderFactory;

    @Inject
    public TransactionResource(TransactionService transactionService,
                               MandateService mandateService, PaymentProviderFactory paymentProviderFactory) {
        this.transactionService = transactionService;
        this.mandateService = mandateService;
        this.paymentProviderFactory = paymentProviderFactory;
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
        Transaction transaction = transactionService.createTransaction(transactionRequest, mandate, accountExternalId);
        TransactionResponse response = transactionService.createPaymentResponseWithAllLinks(transaction, accountExternalId, uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/charges/collect")
    @Produces(APPLICATION_JSON)
    public Response collectPaymentFromMandate(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest, @Context UriInfo uriInfo) {
        LOGGER.info("Received collect payment from mandate request");
        collectPaymentRequestValidator.validate(collectPaymentRequest);
        Transaction transaction = mandateService.collect(gatewayAccount, collectPaymentRequest);
        CollectPaymentResponse response = transactionService.collectPaymentResponseWithSelfLink(transaction, gatewayAccount.getExternalId(), uriInfo);
        return created(response.getLink("self")).entity(response).build();
    }
}
