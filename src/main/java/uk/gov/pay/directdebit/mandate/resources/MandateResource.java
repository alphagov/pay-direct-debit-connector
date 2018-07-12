package uk.gov.pay.directdebit.mandate.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequestValidator;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

@Path("/")
public class MandateResource {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateResource.class);
    private final MandateService mandateService;
    private final CreateMandateRequestValidator createMandateRequestValidator = new CreateMandateRequestValidator();
    private final MandateServiceFactory mandateServiceFactory;

    @Inject
    public MandateResource(MandateService mandateService,
            MandateServiceFactory mandateServiceFactory) {
        this.mandateService = mandateService;
        this.mandateServiceFactory = mandateServiceFactory;
    }
    
    @POST
    @Path("/v1/api/accounts/{accountId}/mandates")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createMandate(@PathParam("accountId") GatewayAccount gatewayAccount, Map<String, String> createMandateRequestMap, @Context UriInfo uriInfo) {
        LOGGER.info("Received create mandate request with gateway account external id - {}", gatewayAccount.getExternalId());
        createMandateRequestValidator.validate(createMandateRequestMap);
        CreateMandateRequest createMandateRequest = CreateMandateRequest.of(createMandateRequestMap);
        CreateMandateResponse createMandateResponse = mandateServiceFactory.getOnDemandMandateService().create(gatewayAccount, createMandateRequest, uriInfo);
        return created(createMandateResponse.getLink("self")).entity(createMandateResponse).build();
    }

    @GET
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getMandate(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId, @Context UriInfo uriInfo) {
        LOGGER.info("Retrieving mandate {} for frontend", mandateExternalId);
        GetMandateResponse response = mandateService.populateGetMandateResponse(accountExternalId, mandateExternalId, uriInfo);
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/v1/accounts/{accountId}/mandates/{mandateExternalId}")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getMandateFrontend(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId) {
        LOGGER.info("Retrieving mandate {} for frontend", mandateExternalId);
        DirectDebitInfoFrontendResponse response = mandateService.populateGetMandateResponseForFrontend(accountExternalId, mandateExternalId);
        return Response.ok(response).build();
    }

    @GET
    @Path("/v1/accounts/{accountId}/mandates/{mandateExternalId}/payments/{transactionExternalId}")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getMandateWithTransactionFrontend(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId, @PathParam("transactionExternalId") String transactionExternalId) {
        LOGGER.info("Retrieving mandate {} and charge {} for frontend", mandateExternalId, transactionExternalId);
        DirectDebitInfoFrontendResponse response = mandateService.populateGetMandateWithTransactionResponseForFrontend(accountExternalId, transactionExternalId);
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/cancel")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response userCancelSetup(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId) {
        LOGGER.info("User wants to cancel setup of mandate with external id - {}", mandateExternalId);
        mandateService.cancelMandateCreation(mandateExternalId);
        return Response.noContent().build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/change-payment-method")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response userChangePaymentMethod(@PathParam("accountId") String accountExternalId, @PathParam("mandateExternalId") String mandateExternalId) {
        LOGGER.info("User wants to change payment method for mandate with external id - {}", mandateExternalId);
        mandateService.changePaymentMethodFor(mandateExternalId);
        return Response.noContent().build();
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/confirm")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response confirm(@PathParam("accountId") GatewayAccount gatewayAccount,
            @PathParam("mandateExternalId") String mandateExternalId,
            Map<String, String> mandateConfirmationRequestMap) {

        LOGGER.info("Confirming direct debit details for mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateServiceFactory
                .getMandateQueryService()
                .findByExternalId(mandateExternalId);
        ConfirmMandateRequest confirmMandateRequest = ConfirmMandateRequest.of(mandateConfirmationRequestMap); 
        mandateServiceFactory.getMandateControlService(mandate.getType())
                .confirm(gatewayAccount, mandate, confirmMandateRequest);
        LOGGER.info("Confirmed direct debit details for mandate with id: {}", mandateExternalId);

        return Response.noContent().build();
    }
}
