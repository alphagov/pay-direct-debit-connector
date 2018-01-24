package uk.gov.pay.directdebit.gatewayaccounts.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.URIBuilder;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class GatewayAccountResource {
    public static final String GATEWAY_ACCOUNT_API_PATH = "/v1/api/accounts/{accountId}";
    public static final String GATEWAY_ACCOUNTS_API_PATH = "/v1/api/accounts";

    private static final String PAYMENT_PROVIDER_KEY = "payment_provider";
    private static final String TYPE_KEY = "type";
    private static final String SERVICE_NAME_KEY = "service_name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String ANALYTICS_ID_KEY = "analytics_id";
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountResource.class);

    private GatewayAccountService gatewayAccountService;

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
    public Response createNewGatewayAccount(JsonNode node, @Context UriInfo uriInfo) {
        //todo gateway account parser? (like for payer)
        String accountType = Optional.ofNullable(node.get(TYPE_KEY))
                .map(JsonNode::textValue)
                .orElse(GatewayAccount.Type.TEST.toString());
        GatewayAccount.Type type = GatewayAccount.Type.fromString(accountType);

        String provider = Optional.ofNullable(node.get(PAYMENT_PROVIDER_KEY))
                .map(JsonNode::textValue)
                .orElse(PaymentProvider.SANDBOX.toString());
        PaymentProvider paymentProvider = PaymentProvider.fromString(provider);

        LOGGER.info("Creating new gateway account using the {} provider pointing to {}", provider, accountType);
        GatewayAccount gatewayAccount = gatewayAccountService.create();
        if (node.has(SERVICE_NAME_KEY)) {
            gatewayAccount.setServiceName(node.get(SERVICE_NAME_KEY).textValue());
        }
        if (node.has(DESCRIPTION_KEY)) {
            gatewayAccount.setDescription(node.get(DESCRIPTION_KEY).textValue());
        }
        if (node.has(ANALYTICS_ID_KEY)) {
            gatewayAccount.setAnalyticsId(node.get(ANALYTICS_ID_KEY).textValue());
        }
        gatewayAccountService.create(gatewayAccount);
        URI newLocation = uriInfo.
                getBaseUriBuilder().
                path("/v1/api/accounts/{accountId}").build(entity.getId());

        Map<String, Object> account = newHashMap();
        account.put("gateway_account_id", String.valueOf(entity.getId()));
        account.put(PROVIDER_ACCOUNT_TYPE, entity.getType());
        account.put(DESCRIPTION_FIELD_NAME, entity.getDescription());
        account.put(SERVICE_NAME_FIELD_NAME, entity.getServiceName());
        account.put(ANALYTICS_ID_FIELD_NAME, entity.getAnalyticsId());

        addSelfLink(newLocation, account);

        return Response.created(newLocation).entity(account).build();
    }
}
