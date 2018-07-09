package uk.gov.pay.directdebit.events.resources;

import uk.gov.pay.directdebit.events.api.DirectDebitEventsResponse;
import uk.gov.pay.directdebit.events.service.DirectDebitEventsSearchService;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/events")
public class DirectDebitEventsResource {
    
    private final DirectDebitEventsSearchService directDebitEventsSearchService;

    @Inject
    public DirectDebitEventsResource(DirectDebitEventsSearchService directDebitEventsSearchService) {
        this.directDebitEventsSearchService = directDebitEventsSearchService;
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response findEvents(@QueryParam("before") String beforeDate,
                               @QueryParam("after") String afterDate,
                               @QueryParam("page_size") Integer pageSize,
                               @QueryParam("page") Integer page,
                               @QueryParam("mandate_id") String mandateId,
                               @QueryParam("transaction_id") String transactionId,
                               @Context UriInfo uriInfo) {

        DirectDebitEventSearchParams searchParams = DirectDebitEventSearchParams.builder()
                .beforeDate(beforeDate)
                .afterDate(afterDate)
                .mandateExternalId(mandateId)
                .transactionExternalId(transactionId)
                .pageSize(pageSize)
                .page(page)
                .build();

        DirectDebitEventsResponse response = directDebitEventsSearchService.search(searchParams, uriInfo);
        
        return Response.ok(response).build();
    }
}
