package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;

import javax.inject.Inject;
import java.util.List;

public class GovUkPayEventService implements AlexAndDanEventService<GovUkPayEvent> {
    
    private final GovUkPayEventDao govUkPayEventDao;

    @Inject
    public GovUkPayEventService(GovUkPayEventDao govUkPayEventDao) {
        this.govUkPayEventDao = govUkPayEventDao;
    }

    @Override
    public void handleEvents(List<GovUkPayEvent> events) {
        events.forEach(govUkPayEventDao::insert);
    }
}
