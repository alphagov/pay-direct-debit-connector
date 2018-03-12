package uk.gov.pay.directdebit.payers.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import javax.inject.Inject;
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
    private static final String PAYER_API_PATH = "/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers/{payerExternalId}";

    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerResource.class);
    private final PaymentProviderFactory paymentProviderFactory;
    private final CreatePayerValidator createPayerValidator = new CreatePayerValidator();
    @Inject
    public PayerResource(PaymentProviderFactory paymentProviderFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/payment-requests/{paymentRequestExternalId}/payers")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPayer(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("paymentRequestExternalId") String paymentRequestExternalId, Map<String, String> createPayerRequest, @Context UriInfo uriInfo) {
        createPayerValidator.validate(paymentRequestExternalId, createPayerRequest);

        LOGGER.info("Received createPayerInProvider request for payment request with id: {}", paymentRequestExternalId);

        DirectDebitPaymentProvider payerService = paymentProviderFactory.getServiceFor(gatewayAccount.getPaymentProvider());
        Payer payer = payerService.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);

        CreatePayerResponse createPayerResponse = CreatePayerResponse.from(payer);

        URI newPayerLocation = URIBuilder.selfUriFor(uriInfo, PAYER_API_PATH, gatewayAccount.getId().toString(), paymentRequestExternalId, createPayerResponse.getPayerExternalId());
        return Response.created(newPayerLocation).entity(createPayerResponse).build();
    }

}
