package uk.gov.pay.directdebit.events.resources;

import uk.gov.pay.directdebit.events.service.DirectDebitEventsSearchService;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.List;

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
                               @QueryParam("page_size") String pageSize,
                               @QueryParam("page") String page,
                               @QueryParam("mandate_id") Long mandateId,
                               @QueryParam("transaction_id") Long transactionId) {

        DirectDebitEventSearchParams searchParams = DirectDebitEventSearchParams.builder()
                .beforeDate(beforeDate)
                .afterDate(afterDate)
                .mandateId(mandateId)
                .transactionId(transactionId)
                .build();
        
        List<DirectDebitEvent> events = directDebitEventsSearchService.search(searchParams);
        
        return Response.ok(events).build();
    }
}
