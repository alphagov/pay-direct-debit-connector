package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.events.api.DirectDebitEventsPagination;
import uk.gov.pay.directdebit.events.api.DirectDebitEventsResponse;
import uk.gov.pay.directdebit.events.dao.DirectDebitEventSearchDao;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class DirectDebitEventsSearchService {
    
    private final DirectDebitEventSearchDao dao;

    @Inject
    public DirectDebitEventsSearchService(DirectDebitEventSearchDao dao) {
        this.dao = dao;
    }

    public DirectDebitEventsResponse search(DirectDebitEventSearchParams searchParams, UriInfo uriInfo) {
        List<DirectDebitEvent> events = dao.findEvents(searchParams);
        int total = dao.getTotalNumberOfEvents(searchParams);
        return new DirectDebitEventsResponse(
                events, 
                searchParams.getPage(), 
                total, 
                new DirectDebitEventsPagination(searchParams, total, uriInfo));
    }
}
