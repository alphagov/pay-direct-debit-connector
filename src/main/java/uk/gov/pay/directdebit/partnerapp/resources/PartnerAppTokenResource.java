package uk.gov.pay.directdebit.partnerapp.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.partnerapp.api.PartnerAppTokenResponse;
import uk.gov.pay.directdebit.partnerapp.services.PartnerAppTokenService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PartnerAppTokenResource {

    private static final String GATEWAY_ACCOUNT_ID_FIELD = "gateway_account_id";
    private static final String REDIRECT_URI_FIELD = "redirect_uri";
    private final PartnerAppTokenService service;

    @Inject
    public PartnerAppTokenResource(PartnerAppTokenService service) {
        this.service = service;
    }

    /**
     * Resource that will respond with a code, a value used in the <code>state</code> field that is sent to
     * GoCardless by selfservice. This code is used to initiate the OAuth journey and then checked on lingking
     * a merchant account to a partner app.
     *
     * @param requestMap Contains a <code>gateway_account_id</code> for which a <code>code</code> is generated,
     *                   also contains a <code>redirect_uri</code> that will be sent to GoCardless with the POST
     *                   request
     * @return 204 Created
     */
    @POST
    @Path("/v1/api/gocardless/partnerapp/tokens")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createGoCardlessPartnerAppConnectTokenState(Map<String, String> requestMap) {
        validateRequestFields(requestMap);
        String gatewayAccountExternalId = requestMap.get(GATEWAY_ACCOUNT_ID_FIELD);
        String redirectUri = requestMap.get(REDIRECT_URI_FIELD);
        return service.createToken(gatewayAccountExternalId, redirectUri).map(
                newEntity -> {
                    PartnerAppTokenResponse response = PartnerAppTokenResponse.from(newEntity);
                    URI location = URI.create("/v1/api/gocardless/partnerapp/tokens/" + response.getToken());
                    return Response.created(location).entity(response).build();
                })
                .orElseThrow(
                        () -> new BadRequestException(
                                format("There is no gateway account with external id [%s]", gatewayAccountExternalId)));
    }

    private void validateRequestFields(Map<String, String> requestMap) {
        List<String> errors = new ArrayList<>(2);
        if (!requestMap.containsKey(GATEWAY_ACCOUNT_ID_FIELD) || null == requestMap.get(GATEWAY_ACCOUNT_ID_FIELD)) {
            errors.add(GATEWAY_ACCOUNT_ID_FIELD);
        }

        if (!requestMap.containsKey(REDIRECT_URI_FIELD) || null == requestMap.get(REDIRECT_URI_FIELD)) {
            errors.add(REDIRECT_URI_FIELD);
        }

        if (!errors.isEmpty()) {
            throw new MissingMandatoryFieldsException(errors);
        }
    }
}
