package uk.gov.pay.directdebit.tokens.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.tokens.api.TokenResponse;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
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
    private final TokenService tokenService;
    private final MandateService mandateService;

    @Inject
    public SecurityTokensResource(TokenService tokenService, MandateService mandateService) {
        this.tokenService = tokenService;
        this.mandateService = mandateService;
    }

    @GET
    @Path("/v1/tokens/{token}/mandate")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response getMandateForToken(@PathParam("token") String token) {
        TokenExchangeDetails exchangeDetails = mandateService.getMandateFor(token);
        TokenResponse response = TokenResponse.from(exchangeDetails.getMandate());
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/v1/tokens/{token}")
    @Timed
    public Response deleteToken(@PathParam("token") String token) {
        tokenService.deleteToken(token);
        return Response.noContent().build();
    }
}
