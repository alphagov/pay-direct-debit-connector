package uk.gov.pay.directdebit.partnerapp.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.partnerapp.api.GoCardlessAppConnectAccountTokenRequestValidator;
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
public class GoCardlessAppConnectAccountTokenResource {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessAppConnectAccountTokenResource.class);

    public static final String CODE_FIELD = "code";
    public static final String STATE_FIELD = "state";
    private final GoCardlessAppConnectAccountService service;

    @Inject
    public GoCardlessAppConnectAccountTokenResource(GoCardlessAppConnectAccountService service) {
        this.service = service;
    }

    /**
     * Resource that will get a code and will exchange it for an access token.
     * The code is issued by GoCardless, but goes to selfservice and then
     * selfservice will request this resource to finish the OAuth journey of linking
     * a GoCardless merchant account to a GOV.UK Pay Partner app
     *
     * @param requestMap A map containing a <code>code</code> issued by GoCardless and a
     *                   <code>state_token</code> which is the code issued by direct-debit-connector
     *                   to validate the request that is received from GoCardless
     * @return 200 OK
     * @see <a href="https://developer.gocardless.com/getting-started/partners/connecting-your-users-accounts/#getting-an-access-token">GoCardless Developers: Getting an access token</a>
     */
    @POST
    @Path("/v1/api/gocardless/partnerapp/tokens")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getGoCardlessConnectAccessToken(Map<String, String> requestMap) {
        GoCardlessAppConnectAccountTokenRequestValidator.validateRequest(requestMap);
        String code = requestMap.get(CODE_FIELD);
        String state = requestMap.get(STATE_FIELD);
        LOGGER.info("Received request to exchange GoCardless connect code [{}] with state [{}] for an access token",
                code, state);
        return service.exchangeCodeForToken(code, state);
    }
}
