package uk.gov.pay.directdebit.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;

import javax.inject.Inject;

public class GoCardlessEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessEventService.class);

    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final GoCardlessEventDao goCardlessEventDao;

    @Inject
    public GoCardlessEventService(GoCardlessPaymentDao goCardlessPaymentDao,
            GoCardlessMandateDao goCardlessMandateDao,
            GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
        this.goCardlessEventDao = goCardlessEventDao;
    }

    public void storeEvent(GoCardlessEvent event) {
        goCardlessEventDao.insert(event);
        LOGGER.info("Inserted gocardless event with gocardless event id {} ", event.getGoCardlessEventId());
    }

    public GoCardlessPayment findPaymentForEvent(GoCardlessEvent event) {
        return goCardlessPaymentDao
                .findByEventResourceId(event.getPaymentId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless payment for event: {}", event.getJson());
                    return new GoCardlessPaymentNotFoundException(event.getPaymentId());
                });
    }

    public GoCardlessMandate findGoCardlessMandateForEvent(GoCardlessEvent event) {
        return goCardlessMandateDao
                .findByEventResourceId(event.getMandateId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for event: {}", event.getJson());
                    return new GoCardlessMandateNotFoundException("resource id", event.getMandateId());
                });
    }
}
