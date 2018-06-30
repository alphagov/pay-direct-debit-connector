package uk.gov.pay.directdebit.mandate.resources;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.noContent;

@Path("/")
public class ConfirmMandateSetupResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmMandateSetupResource.class);
    private final MandateService mandateService;

    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();

    @Inject
    public ConfirmMandateSetupResource(MandateService mandateService) {
        this.mandateService = mandateService;
    }
    
    @POST
    @Path("/v1/api/accounts/{accountId}/mandates/{mandateExternalId}/confirm")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response confirm(@PathParam("accountId") GatewayAccount gatewayAccount, @PathParam("mandateExternalId") String mandateExternalId, Map<String, String> confirmDetailsRequest) {
        LOGGER.info("Confirming direct debit details for mandate with id: {}", mandateExternalId);
        bankAccountDetailsValidator.validate(confirmDetailsRequest);
        mandateService.confirm(gatewayAccount, mandateExternalId, confirmDetailsRequest);
        LOGGER.info("Confirmed direct debit details for mandate with id: {}", mandateExternalId);

        return noContent().build();
    }
}
