package uk.gov.pay.directdebit.tokens.resources;

import uk.gov.pay.directdebit.tokens.api.TokenResponse;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class SecurityTokensResource {
    static final String TOKEN_PATH = "/v1/tokens/{token}";
    private static final String PAYMENT_REQUEST_BY_TOKEN_PATH = TOKEN_PATH + "/payment-request";

    private final TokenService tokenService;

    @Inject
    public SecurityTokensResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GET
    @Path(PAYMENT_REQUEST_BY_TOKEN_PATH)
    @Produces(APPLICATION_JSON)
    public Response getPaymentRequestForToken(@PathParam("token") String token) {
        TokenResponse response = TokenResponse.from(tokenService.validateChargeWithToken(token));
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path(TOKEN_PATH)
    public Response deleteToken(@PathParam("token") String token) {
        tokenService.deleteToken(token);
        return Response.noContent().build();
    }
}
