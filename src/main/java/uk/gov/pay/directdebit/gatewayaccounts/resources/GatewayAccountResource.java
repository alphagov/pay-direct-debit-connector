package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.gatewayaccounts.api.CreateGatewayAccountValidator;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.services.GatewayAccountService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class GatewayAccountResource {
    static final String GATEWAY_ACCOUNT_API_PATH = "/v1/api/accounts/{accountId}";
    static final String GATEWAY_ACCOUNTS_API_PATH = "/v1/api/accounts";

    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountResource.class);

    private GatewayAccountService gatewayAccountService;
    private final CreateGatewayAccountValidator createGatewayAccountValidator = new CreateGatewayAccountValidator();


    public GatewayAccountResource(GatewayAccountService gatewayAccountService) {
        this.gatewayAccountService = gatewayAccountService;
    }

    @GET
    @Path(GATEWAY_ACCOUNT_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getGatewayAccount(@PathParam("accountId") Long accountId) {
        LOGGER.debug("Getting gateway account for account id {}", accountId);
        GatewayAccountResponse gatewayAccountResponse = GatewayAccountResponse.from(gatewayAccountService.getGatewayAccount(accountId));
        return Response.ok().entity(gatewayAccountResponse).build();
    }

    @GET
    @Path(GATEWAY_ACCOUNTS_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getGatewayAccounts(@Context UriInfo uriInfo) {
        LOGGER.debug("Getting all gateway accounts");
        List<GatewayAccountResponse> gatewayAccounts = gatewayAccountService.getAllGatewayAccounts()
                .stream()
                .map(gatewayAccount -> {
                    GatewayAccountResponse gatewayAccountResponse = GatewayAccountResponse.from(gatewayAccount);
                    gatewayAccountResponse.addLink("self", URIBuilder.selfUriFor(
                            uriInfo,
                            GATEWAY_ACCOUNT_API_PATH,
                            gatewayAccount.getId().toString()));
                    return gatewayAccountResponse;
                })
                .collect(Collectors.toList());
        return Response
                .ok(ImmutableMap.of("accounts", gatewayAccounts))
                .build();
    }

    @POST
    @Path(GATEWAY_ACCOUNTS_API_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createNewGatewayAccount(Map<String, String> request, @Context UriInfo uriInfo) {
        createGatewayAccountValidator.validate(request);
        GatewayAccount gatewayAccount = gatewayAccountService.create(request);
        GatewayAccountResponse gatewayAccountResponse = GatewayAccountResponse.from(gatewayAccount);
        URI selfUrl = URIBuilder.selfUriFor(
                uriInfo,
                GATEWAY_ACCOUNT_API_PATH,
                gatewayAccount.getId().toString());
        gatewayAccountResponse.addLink("self", selfUrl);
        return Response.created(selfUrl).entity(gatewayAccountResponse).build();
    }
}
