package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.exception.GovUkPayEventHasNoMandateIdException;
import uk.gov.pay.directdebit.events.exception.GovUkPayEventHasNoPaymentIdException;
import uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventStateGraph;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException.invalidInitialEventException;
import static uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException.invalidTransitionException;

public class GovUkPayEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GovUkPayEventService.class);

    private final GovUkPayEventDao govUkPayEventDao;
    private final GovUkPayEventStateGraph govUkPayEventStateGraph;

    @Inject
    public GovUkPayEventService(GovUkPayEventDao govUkPayEventDao,
                                GovUkPayEventStateGraph govUkPayEventStateGraph) {
        this.govUkPayEventDao = govUkPayEventDao;
        this.govUkPayEventStateGraph = govUkPayEventStateGraph;
    }

    public void storeEvent(GovUkPayEvent event) {
        if (event.getResourceType() == GovUkPayEvent.ResourceType.MANDATE) {
            storeEventForMandate(event);
        } else {
            storeEventForPayment(event);
        }
    }

    private void storeEventForMandate(GovUkPayEvent event) {
        Long mandateId = event.getMandateId().orElseThrow(() -> new GovUkPayEventHasNoMandateIdException(event.getId()));
        govUkPayEventDao.findLatestEventForMandate(mandateId)
                .ifPresentOrElse(latestEvent -> validateEventTransition(event, latestEvent),
                        () -> validateInitialEvent(event));
        
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event for mandate {}", mandateId);
    }

    private void storeEventForPayment(GovUkPayEvent event) {
        Long paymentId = event.getPaymentId().orElseThrow(() -> new GovUkPayEventHasNoPaymentIdException(event.getId()));
        govUkPayEventDao.findLatestEventForPayment(paymentId)
                .ifPresentOrElse(latestEvent -> validateEventTransition(event, latestEvent),
                        () -> validateInitialEvent(event));
        
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event for payment {}", paymentId);
    }

    private void validateInitialEvent(GovUkPayEvent event) {
        if (!govUkPayEventStateGraph.isValidStartValue(event.getEventType())) {
            throw invalidInitialEventException(event);
        }
    }

    private void validateEventTransition(GovUkPayEvent event, GovUkPayEvent latestEvent) {
        if (!govUkPayEventStateGraph.isValidTransition(latestEvent.getEventType(), event.getEventType())) {
            throw invalidTransitionException(event, latestEvent);
        }
    }
}
