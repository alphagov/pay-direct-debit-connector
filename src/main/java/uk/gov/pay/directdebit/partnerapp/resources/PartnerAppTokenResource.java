package uk.gov.pay.directdebit.partnerapp.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.NonEmptyStringParam;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.validation.MissingMandatoryFieldsException;
import uk.gov.pay.directdebit.partnerapp.api.PartnerAppTokenResponse;
import uk.gov.pay.directdebit.partnerapp.services.PartnerAppTokenService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class PartnerAppTokenResource {

    private static final String GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private final PartnerAppTokenService service;

    @Inject
    public PartnerAppTokenResource(PartnerAppTokenService service) {
        this.service = service;
    }

    @POST
    @Path("/v1/api/gocardless/partnerapp/tokens")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createGoCardlessPartnerAppConnectTokenState(Map<String, String> requestMap) {
        validateGatewayAccountField(requestMap);
        String gatewayAccountExternalId = requestMap.get(GATEWAY_ACCOUNT_ID);
        return service.createToken(gatewayAccountExternalId).map(
                newEntity -> {
                    PartnerAppTokenResponse response = PartnerAppTokenResponse.from(newEntity);
                    URI location = URI.create("/v1/api/gocardless/partnerapp/tokens/" + response.getToken());
                    return Response.created(location).entity(response).build();
                })
                .orElseThrow(
                        () -> new BadRequestException(
                                format("There is no gateway account with external id [%s]", gatewayAccountExternalId)));
    }

    @POST
    @Path("/v1/api/gocardless/partnerapp/tokens/{token}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response disableGoCardlessPartnerAppConnectTokenState(@PathParam("token") NonEmptyStringParam token, Map<String, String> requestMap) {
        validateGatewayAccountField(requestMap);
        String gatewayAccountExternalId = requestMap.get(GATEWAY_ACCOUNT_ID);

        return service.disableToken(token.get().toString(), gatewayAccountExternalId)
                .map(entityId -> Response.ok().build())
                .orElseThrow(
                        () -> new BadRequestException(
                                format("There was an error disabling token [%s] for gateway account [%s]",
                                        token, gatewayAccountExternalId)));
    }

    @GET
    @Path("/v1/api/gocardless/partnerapp/tokens/{token}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getGoCardlessPartnerAppConnectTokenState(@PathParam("token") NonEmptyStringParam token,
                                                             @QueryParam("gatewayAccountId") NonEmptyStringParam gatewayAccountExternalId) {
        return service.findByTokenAndGatewayAccountId(token.get().toString(), gatewayAccountExternalId.get().toString())
                .map(entity -> Response.ok(PartnerAppTokenResponse.from(entity)).build())
                .orElseThrow(
                        () -> new BadRequestException(
                                format("There was an error disabling token [%s] for gateway account [%s]",
                                        token, gatewayAccountExternalId.get().toString())));
    }

    private void validateGatewayAccountField(Map<String, String> requestMap) {
        if (!requestMap.containsKey(GATEWAY_ACCOUNT_ID) || null == requestMap.get(GATEWAY_ACCOUNT_ID)) {
            throw new MissingMandatoryFieldsException(Collections.singletonList(GATEWAY_ACCOUNT_ID));
        }
    }
}
