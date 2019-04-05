package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.api.CreateGatewayAccountRequest;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class GatewayAccountResource {
    public static final String GATEWAY_ACCOUNT_API_PATH = "/v1/api/accounts/{accountId}";
    static final String GATEWAY_ACCOUNTS_API_PATH = "/v1/api/accounts";
    static final String GATEWAY_ACCOUNTS_FRONTEND_PATH = "/v1/frontend/accounts";

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayAccountResource.class);

    private GatewayAccountService gatewayAccountService;
    @Inject
    public GatewayAccountResource(GatewayAccountService gatewayAccountService) {
        this.gatewayAccountService = gatewayAccountService;
    }

    @GET
    @Path(GATEWAY_ACCOUNT_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getGatewayAccount(@PathParam("accountId") String accountExternalId) {
        LOGGER.debug("Getting gateway account for account external id {}", accountExternalId);
        GatewayAccountResponse gatewayAccountResponse = GatewayAccountResponse.from(gatewayAccountService.getGatewayAccountForId(accountExternalId));
        return Response.ok().entity(gatewayAccountResponse).build();
    }

    @GET
    @Path(GATEWAY_ACCOUNTS_API_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getApiGatewayAccounts(@DefaultValue("") @QueryParam("externalAccountIds") String externalAccountIdsArg,
                                          @Context UriInfo uriInfo) {
        LOGGER.debug("Getting all api gateway accounts");
        return getGatewayAccounts(externalAccountIdsArg, uriInfo);
    }

    @GET
    @Path(GATEWAY_ACCOUNTS_FRONTEND_PATH)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getFrontendGatewayAccounts(@DefaultValue("") @QueryParam("externalAccountIds") String externalAccountIdsArg,
                                               @Context UriInfo uriInfo) {
        LOGGER.debug("Getting all frontend gateway accounts");
        return getGatewayAccounts(externalAccountIdsArg, uriInfo);
    }

    @POST
    @Path(GATEWAY_ACCOUNTS_API_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response createNewGatewayAccount(@Valid CreateGatewayAccountRequest request, @Context UriInfo uriInfo) {
        GatewayAccount gatewayAccount = gatewayAccountService.create(request);
        GatewayAccountResponse gatewayAccountResponse = GatewayAccountResponse.from(gatewayAccount).withSelfLink(uriInfo);
        return Response.created(gatewayAccountResponse.getSelfLink()).entity(gatewayAccountResponse).build();
    }
    
    @PATCH
    @Path(GATEWAY_ACCOUNT_API_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Timed
    public Response updateGatewayAccount(@PathParam("accountId") String accountExternalId, List<Map<String, String>> request) {
        gatewayAccountService.patch(accountExternalId, request);
        return Response.noContent().build();
    }
    
    private Response getGatewayAccounts(String externalAccountIdsArg, UriInfo uriInfo) {
        return Response
                .ok(ImmutableMap.of("accounts", gatewayAccountService.getAllGatewayAccounts(externalAccountIdsArg, uriInfo)))
                .build();
    }
}
