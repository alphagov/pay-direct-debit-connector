package uk.gov.pay.directdebit.tokens.resources;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.tokens.api.TokenResponse;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class SecurityTokensResource {
    static final String TOKEN_PATH = "/v1/tokens/{chargeTokenId}";
    private static final String CHARGE_BY_TOKEN_PATH = TOKEN_PATH + "/charge";

    private final Logger logger = PayLoggerFactory.getLogger(SecurityTokensResource.class);
    private final TokenService tokenService;

    public SecurityTokensResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GET
    @Path(CHARGE_BY_TOKEN_PATH)
    @Produces(APPLICATION_JSON)
    public Response getPaymentRequestForToken(@PathParam("chargeTokenId") String chargeTokenId) {
        TokenResponse response = TokenResponse.from(tokenService.validateChargeWithToken(chargeTokenId));
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path(TOKEN_PATH)
    public Response deleteToken(@PathParam("chargeTokenId") String chargeTokenId) {
        tokenService.deleteToken(chargeTokenId);
        return Response.noContent().build();
    }
}
