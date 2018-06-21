package uk.gov.pay.directdebit.tasks.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.tasks.services.ExpireService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class ExpireResource {

    private final String EXPIRE_SWEEP_API_PATH = "/v1/api/tasks/expire-payments-and-mandates";
    private final ExpireService expireService;
    
    @Inject
    public ExpireResource(ExpireService expireService) {
        this.expireService = expireService;
    }

    @POST
    @Path(EXPIRE_SWEEP_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response expirePaymentsAndMandates() {
        int numberOfExpiredPayments = expireService.expirePayments();
        ResourceResponse resourceResponse =  new ResourceResponse(numberOfExpiredPayments);
        return Response.ok(resourceResponse).build();
    }
    
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private static class ResourceResponse {
        
        @JsonProperty
        private final int numberOfExpiredPayments;

        ResourceResponse(int numberOfExpiredPayments) {
            this.numberOfExpiredPayments = numberOfExpiredPayments;
        }
    }
    
}
