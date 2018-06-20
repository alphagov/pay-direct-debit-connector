package uk.gov.pay.directdebit.tasks.resources;

import uk.gov.pay.directdebit.tasks.services.ExpireService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class ExpireResource {

    private final String EXPIRE_SWEEP_API_PATH = "/v1/api/tasks/expirePaymentsAndMandates";
    private final ExpireService expireService;
    
    @Inject
    public ExpireResource(ExpireService expireService) {
        this.expireService = expireService;
    }

    @POST
    @Path(EXPIRE_SWEEP_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response expirePaymentsAndMandates() {
        expireService.expireMandates();
        expireService.expirePayments();
        return Response.ok().build();
    }
    
}
