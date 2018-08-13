package uk.gov.pay.directdebit.partnerapp.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.partnerapp.api.GoCardlessAppConnectAccountStateRequestValidator;
import uk.gov.pay.directdebit.partnerapp.services.GoCardlessAppConnectAccountService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class GoCardlessAppConnectAccountStateResource {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessAppConnectAccountStateResource.class);

    public static final String GATEWAY_ACCOUNT_ID_FIELD = "gateway_account_id";
    public static final String REDIRECT_URI_FIELD = "redirect_uri";
    private final GoCardlessAppConnectAccountService service;

    @Inject
    public GoCardlessAppConnectAccountStateResource(GoCardlessAppConnectAccountService service) {
        this.service = service;
    }

    /**
     * Resource that will respond with a random string, a value used in the <code>state</code> field that is sent to
     * GoCardless by selfservice. This state is used to initiate the OAuth journey and then checked on linking
     * a merchant account to a partner app.
     *
     * @param requestMap Contains a <code>gateway_account_id</code> for which a <code>state</code> is generated,
     *                   also contains a <code>redirect_uri</code> that will be sent to GoCardless with the POST
     *                   request
     * @return 204 Created
     */
    @POST
    @Path("/v1/api/gocardless/partnerapp/states")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createGoCardlessPartnerAppConnectTokenState(Map<String, String> requestMap) {
        GoCardlessAppConnectAccountStateRequestValidator.validateRequest(requestMap);
        String gatewayAccountExternalId = requestMap.get(GATEWAY_ACCOUNT_ID_FIELD);
        String redirectUri = requestMap.get(REDIRECT_URI_FIELD);
        LOGGER.info("Received request to create a state token for partner app for gateway external id [{}] and redirect url [{}]",
                gatewayAccountExternalId, redirectUri);
        return service.createToken(gatewayAccountExternalId, redirectUri);
    }
}
