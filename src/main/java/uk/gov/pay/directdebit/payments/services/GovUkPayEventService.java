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
import java.util.Optional;

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
        switch (event.getResourceType()) {
            case PAYMENT:
                storeEventForPayment(event);
                break;
            case MANDATE:
                storeEventForMandate(event);
                break;
        }
    }

    public void storeEventForMandate(GovUkPayEvent event) {
        Long mandateId = event.getMandateId().orElseThrow(() -> new GovUkPayEventHasNoMandateIdException(event.getId()));
        if (!isMandateEventValid(event, mandateId)) {
            throw new InvalidGovUkPayEventInsertionException()
        }
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event for mandate {}", mandateId);
    }
    
    public void storeEventForPayment(GovUkPayEvent event) {
        Long paymentId = event.getPaymentId().orElseThrow(() -> new GovUkPayEventHasNoPaymentIdException(event.getId()));
        if (!isPaymentEventValid(event, paymentId)) {
            
        };
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event for payment {}", paymentId);
    }

    private boolean isMandateEventValid(GovUkPayEvent event, Long mandateId) {
        Optional<GovUkPayEvent> maybeLatestEvent = govUkPayEventDao.findLatestEventForMandate(mandateId);
        return maybeLatestEvent
                .map(latestEvent -> govUkPayEventStateGraph.isValidTransition(latestEvent.getEventType(), event.getEventType()))
                .orElse(event.getEventType() == GovUkPayEvent.GovUkPayEventType.MANDATE_CREATED);
    }

    private boolean isPaymentEventValid(GovUkPayEvent event, Long paymentId) {
        Optional<GovUkPayEvent> maybeLatestEvent = govUkPayEventDao.findLatestEventForPayment(paymentId);
        
        // currently there is only one event type for payments, so if an event already exists, new event is invalid
        return maybeLatestEvent
                .map(latestEvent -> false)
                .orElse(event.getEventType() == GovUkPayEvent.GovUkPayEventType.PAYMENT_SUBMITTED);
    }
}
