package uk.gov.pay.directdebit.tokens.resources;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import uk.gov.pay.directdebit.mandate.exception.TransactionConflictException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.api.TokenResponse;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class SecurityTokensResource {
    private final TokenService tokenService;
    private final MandateService mandateService;
    private final TransactionService transactionService;
    @Inject
    public SecurityTokensResource(TokenService tokenService,
            MandateService mandateService,
            TransactionService transactionService) {
        this.tokenService = tokenService;
        this.mandateService = mandateService;
        this.transactionService = transactionService;
    }


    @GET
    @Path("/v1/tokens/{token}/mandate")
    @Produces(APPLICATION_JSON)
    public Response getMandateForToken(@PathParam("token") String token) {
        Mandate mandate = mandateService.validateMandateWithToken(token);
        String transactionExternalId = null;
        if (mandate.getType().equals(MandateType.ONE_OFF)) {
            List<Transaction> transactionsForMandate = transactionService
                    .findTransactionsForMandate(mandate.getExternalId());
            if (transactionsForMandate.size() > 1) {
                throw new TransactionConflictException("Found multiple transactions for one off mandate with external id " + mandate.getExternalId());
            }
            transactionExternalId = transactionsForMandate.get(0).getExternalId();
        }
        TokenResponse response = TokenResponse.from(mandate, transactionExternalId);
        return Response.ok().entity(response).build();
    }

    @DELETE
    @Path("/v1/tokens/{token}")
    public Response deleteToken(@PathParam("token") String token) {
        tokenService.deleteToken(token);
        return Response.noContent().build();
    }
}
