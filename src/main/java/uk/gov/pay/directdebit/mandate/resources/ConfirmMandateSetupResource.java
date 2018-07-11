package uk.gov.pay.directdebit.mandate.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationRequest;
import uk.gov.pay.directdebit.mandate.services.MandateServiceFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.noContent;

@Path("/")
public class ConfirmMandateSetupResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmMandateSetupResource.class);
    private final MandateServiceFactory mandateServiceFactory;

    @Inject
    public ConfirmMandateSetupResource(MandateServiceFactory mandateServiceFactory) {
        this.mandateServiceFactory = mandateServiceFactory;
    }
    
    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/confirm")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response confirm(
            @PathParam("accountId") GatewayAccount gatewayAccount,
            @PathParam("mandateExternalId") String mandateExternalId,
            MandateConfirmationRequest mandateConfirmationRequest) {

        LOGGER.info("Confirming direct debit details for mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateServiceFactory
                .getMandateQueryService()
                .findByExternalId(mandateExternalId);
        mandateServiceFactory.getMandateControlService(mandate)
                .confirm(gatewayAccount, mandate, mandateConfirmationRequest);
        LOGGER.info("Confirmed direct debit details for mandate with id: {}", mandateExternalId);

        return noContent().build();
    }
}
