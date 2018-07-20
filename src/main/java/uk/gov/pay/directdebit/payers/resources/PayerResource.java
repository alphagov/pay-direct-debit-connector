package uk.gov.pay.directdebit.payers.resources;

import java.net.URI;
import java.util.Map;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

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
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerResponse;
import uk.gov.pay.directdebit.payers.api.CreatePayerValidator;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PayerResource {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerResource.class);
    private final PaymentProviderFactory paymentProviderFactory;
    private final CreatePayerValidator createPayerValidator = new CreatePayerValidator();
    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();
    private final PayerService payerService;
    private final MandateServiceFactory mandateServiceFactory;

    @Inject
    public PayerResource(PaymentProviderFactory paymentProviderFactory,
            PayerService payerService,
            MandateServiceFactory mandateServiceFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.payerService = payerService;
        this.mandateServiceFactory = mandateServiceFactory;
    }
    
    @PUT
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createPayer(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("mandateExternalId") MandateExternalId mandateExternalId, Map<String, String> createPayerRequest, @Context UriInfo uriInfo) {
        createPayerValidator.validate(mandateExternalId.toString(), createPayerRequest);

        LOGGER.info("Received create payer request for mandate with id: {}", mandateExternalId);

        Payer payer = payerService.createOrUpdatePayer(mandateExternalId, createPayerRequest);

        CreatePayerResponse createPayerResponse = CreatePayerResponse.from(payer);

        URI newPayerLocation = URIBuilder.selfUriFor(uriInfo,
                "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/{payerExternalId}",
                gatewayAccount.getExternalId(), mandateExternalId.toString(), createPayerResponse.getPayerExternalId());
        return Response.created(newPayerLocation).entity(createPayerResponse).build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/payers/bank-account/validate")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Timed
    public Response validateBankAccount(
            @PathParam("accountId") GatewayAccount gatewayAccount,
            @PathParam("mandateExternalId") MandateExternalId mandateExternalId,
            Map<String, String> bankAccountDetails) {
        bankAccountDetailsValidator.validate(bankAccountDetails);
        LOGGER.info("Validating bank account details for mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateServiceFactory
                .getMandateQueryService()
                .findByExternalId(mandateExternalId);        
        
        BankAccountValidationResponse response = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .validate(mandate, BankAccountDetails.of(bankAccountDetails));
        LOGGER.info("Bank account details are valid: {}, mandate with id: {}", response.isValid(), mandateExternalId);
        return Response.ok(response).build();
    }
}
