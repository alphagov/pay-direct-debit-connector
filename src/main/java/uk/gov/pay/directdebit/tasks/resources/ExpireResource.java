package uk.gov.pay.directdebit.tasks.resources;

import com.codahale.metrics.annotation.Timed;
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
    
    private final ExpireService expireService;
    
    @Inject
    public ExpireResource(ExpireService expireService) {
        this.expireService = expireService;
    }

    @POST
    @Path("/v1/api/tasks/expire-payments-and-mandates")
    @Produces(APPLICATION_JSON)
    @Timed
    public Response expirePaymentsAndMandates() {
        int numberOfExpiredPayments = expireService.expirePayments();
        int numberOfExpiredMandates = expireService.expireMandates();
        ResourceResponse resourceResponse =  new ResourceResponse(numberOfExpiredPayments, numberOfExpiredMandates);
        return Response.ok(resourceResponse).build();
    }
    
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    private static class ResourceResponse {
        
        @JsonProperty
        private final int numberOfExpiredPayments;
        
        @JsonProperty
        private final int numberOfExpiredMandates;

        ResourceResponse(int numberOfExpiredPayments, int numberOfExpiredMandates) {
            this.numberOfExpiredPayments = numberOfExpiredPayments;
            this.numberOfExpiredMandates = numberOfExpiredMandates;
        }
    }
    
}
