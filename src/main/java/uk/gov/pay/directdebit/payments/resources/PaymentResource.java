package uk.gov.pay.directdebit.payments.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequestValidator;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.CollectService;
import uk.gov.pay.directdebit.payments.services.PaymentService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_CREATED;

@Path("/")
public class PaymentResource {
    //has to be /charges unless we change public api
    public static final String CHARGE_API_PATH = "/v1/api/accounts/{accountId}/charges/{paymentExternalId}";

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);
    
    private final PaymentService paymentService;
    private final CollectService collectService;
    private final CollectPaymentRequestValidator collectPaymentRequestValidator = new CollectPaymentRequestValidator();

    @Inject
    public PaymentResource(PaymentService paymentService, CollectService collectService) {
        this.paymentService = paymentService;
        this.collectService = collectService;
    }

    @GET
    @Path(CHARGE_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getCharge(@PathParam("paymentExternalId") String transactionExternalId) {
        CollectPaymentResponse response = paymentService.getPaymentWithExternalId(transactionExternalId);
        return Response.ok(response).build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/charges/collect")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response collectPaymentFromMandate(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequestMap) {
        LOGGER.info("Received collect payment from mandate request");
        collectPaymentRequestValidator.validate(collectPaymentRequestMap);
        Payment paymentToCollect = collectService.collect(gatewayAccount, CollectPaymentRequest.of(collectPaymentRequestMap));
        CollectPaymentResponse response = CollectPaymentResponse.from(paymentToCollect);
        return Response.status(SC_CREATED).entity(response).build();
    }
}
