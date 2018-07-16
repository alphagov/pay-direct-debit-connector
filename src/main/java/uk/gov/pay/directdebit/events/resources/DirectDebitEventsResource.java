package uk.gov.pay.directdebit.events.resources;

import com.codahale.metrics.annotation.Timed;
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
    @Timed
    public Response findEvents(@QueryParam("to_date") String toDate,
                               @QueryParam("from_date") String fromDate,
                               @QueryParam("display_size") Integer displaySize,
                               @QueryParam("page") Integer page,
                               @QueryParam("mandate_external_id") String mandateId,
                               @QueryParam("transaction_external_id") String transactionId,
                               @Context UriInfo uriInfo) {

        DirectDebitEventSearchParams searchParams = new DirectDebitEventSearchParams.DirectDebitEventSearchParamsBuilder()
                .toDate(toDate)
                .fromDate(fromDate)
                .mandateExternalId(mandateId)
                .transactionExternalId(transactionId)
                .pageSize(displaySize)
                .page(page)
                .build();

        DirectDebitEventsResponse response = directDebitEventsSearchService.search(searchParams, uriInfo);
        
        return Response.ok(response).build();
    }
}
