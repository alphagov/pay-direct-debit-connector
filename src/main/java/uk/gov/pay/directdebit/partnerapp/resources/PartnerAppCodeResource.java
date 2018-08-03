package uk.gov.pay.directdebit.partnerapp.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.partnerapp.services.PartnerAppTokenService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PartnerAppCodeResource {

    private static final String ACCESS_CODE = "access_code";
    private static final String PARTNER_STATE = "partner_state";
    private final PartnerAppTokenService service;

    @Inject
    public PartnerAppCodeResource(PartnerAppTokenService service) {
        this.service = service;
    }

    /**
     * Resource that will get an access code and will exchange it for an access token.
     * The access code is issued by GoCardless, but goes to selfservice and then
     * selfservice will request this resource to finish the OAuth journey of linking
     * a GoCardless merchant account to a GOV.UK Pay Partner app
     *
     * @param requestMap A map containing an access_code issued by GoCardless and a
     *                   partner_state which the code issued by direct-debit-connector
     *                   to validate the request that is received from GoCardless
     * @return 200 OK
     */
    @POST
    @Path("/v1/api/gocardless/partnerapp/codes")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createGoCardlessPartnerAppConnectTokenState(Map<String, String> requestMap) {
        validateRequestFields(requestMap);
        String accessCode = requestMap.get(ACCESS_CODE);
        String partnerState = requestMap.get(PARTNER_STATE);
        return service.exchangeCodeForToken(accessCode, partnerState);
    }

    private void validateRequestFields(Map<String, String> requestMap) {
        List<String> errors = new ArrayList<>();
        if (!requestMap.containsKey(ACCESS_CODE) || null == requestMap.get(ACCESS_CODE)) {
            errors.add(ACCESS_CODE);
        }

        if (!requestMap.containsKey(PARTNER_STATE) || null == requestMap.get(PARTNER_STATE)) {
            errors.add(PARTNER_STATE);
        }

        if (!errors.isEmpty()) {
            throw new MissingMandatoryFieldsException(errors);
        }
    }
}
