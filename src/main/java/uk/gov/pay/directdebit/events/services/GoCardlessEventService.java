package uk.gov.pay.directdebit.events.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;

import javax.inject.Inject;
import java.util.List;

public class GoCardlessEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessEventService.class);

    private final GoCardlessEventDao goCardlessEventDao;

    @Inject
    public GoCardlessEventService(GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
    }

    public void storeEvents(List<GoCardlessEvent> event) {
        goCardlessEventDao.insert(event);
    }

}
