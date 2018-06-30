package uk.gov.pay.directdebit.payers.resources;

import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PayerResource {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerResource.class);
    private final PayerService payerService;
    private final MandateService mandateService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final CreatePayerValidator createPayerValidator = new CreatePayerValidator();
    @Inject
    public PayerResource(PayerService payerService, MandateService mandateService, PaymentProviderFactory paymentProviderFactory) {
        this.payerService = payerService;
        this.mandateService = mandateService;
        this.paymentProviderFactory = paymentProviderFactory;
    }
    
    @PUT
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createPayer(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("mandateExternalId") String mandateExternalId, Map<String, String> createPayerRequest, @Context UriInfo uriInfo) {
        createPayerValidator.validate(mandateExternalId, createPayerRequest);

        LOGGER.info("Received create payer request for mandate with id: {}", mandateExternalId);

        Payer payer = payerService.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);

        CreatePayerResponse createPayerResponse = CreatePayerResponse.from(payer);

        URI newPayerLocation = URIBuilder.selfUriFor(uriInfo,
                "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/{payerExternalId}",
                gatewayAccount.getExternalId(), mandateExternalId, createPayerResponse.getPayerExternalId());
        return Response.created(newPayerLocation).entity(createPayerResponse).build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/bank-account/validate")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response validateBankAccount(
            @PathParam("accountId") GatewayAccount gatewayAccount,
            @PathParam("mandateExternalId") String mandateExternalId,
            Map<String, String> bankAccountDetails) {
        LOGGER.info("Validating bank account details for mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateService.findByExternalId(mandateExternalId);
                
        DirectDebitPaymentProvider payerService = paymentProviderFactory.getServiceFor(
                gatewayAccount.getPaymentProvider(), mandate.getType()
                
        );
        BankAccountValidationResponse response = payerService.validate(mandateExternalId, bankAccountDetails);
        LOGGER.info("Bank account details are valid: {}, mandate with id: {}", response.isValid(), mandateExternalId);
        return Response.ok(response).build();
    }
}
