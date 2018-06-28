package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.events.dao.DirectDebitEventSearchDao;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.params.DirectDebitEventSearchParams;

import javax.inject.Inject;
import java.util.List;

public class DirectDebitEventsSearchService {
    
    private final DirectDebitEventSearchDao dao;

    @Inject
    public DirectDebitEventsSearchService(DirectDebitEventSearchDao dao) {
        this.dao = dao;
    }

    public List<DirectDebitEvent> search(DirectDebitEventSearchParams searchParams) {
        return dao.findEvents(searchParams);
    }
}
