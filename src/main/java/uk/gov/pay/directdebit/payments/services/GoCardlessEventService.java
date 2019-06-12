package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import javax.inject.Inject;

public class GoCardlessEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessEventService.class);

    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final GoCardlessEventDao goCardlessEventDao;

    @Inject
    public GoCardlessEventService(GoCardlessPaymentDao goCardlessPaymentDao,
                                  GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessPaymentDao = goCardlessPaymentDao;
        this.goCardlessEventDao = goCardlessEventDao;
    }

    public void storeEvent(GoCardlessEvent event) {
        goCardlessEventDao.insert(event);
        LOGGER.info("Inserted gocardless event with gocardless event id {} ", event.getGoCardlessEventId());
    }

    public void updateInternalEventId(GoCardlessEvent event) {
        goCardlessEventDao.updateEventId(event.getId(), event.getEventId());
        LOGGER.info("Updated gocardless event with gocardless event id {}, internal event id {} ", event.getGoCardlessEventId(), event.getEventId());
    }

    public GoCardlessPayment findPaymentForEvent(GoCardlessEvent event) {
        return goCardlessPaymentDao
                .findByEventResourceId(event.getResourceId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless payment for event: {}", event.getJson());
                    return new GoCardlessPaymentNotFoundException(event.getResourceId());
                });
    }

}
