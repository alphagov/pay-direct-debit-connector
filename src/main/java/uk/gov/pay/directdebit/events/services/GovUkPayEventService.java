package uk.gov.pay.directdebit.events.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.exception.InvalidGovUkPayEventInsertionException;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventStateGraph;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Payment;

import javax.inject.Inject;


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

    public void storeEventForMandate(Mandate mandate, GovUkPayEventType eventType) {
        var event = new GovUkPayEvent(mandate, eventType);
        govUkPayEventDao.findLatestEventForMandate(mandate.getId())
                .ifPresentOrElse(latestEvent -> validateEventTransition(event, latestEvent),
                        () -> validateInitialEvent(event));
        
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event of type {} for mandate {}", eventType, mandate.getExternalId());
    }

    public void storeEventForPayment(Payment payment, GovUkPayEventType eventType) {
        var event = new GovUkPayEvent(payment, eventType);
        govUkPayEventDao.findLatestEventForPayment(payment.getId())
                .ifPresentOrElse(latestEvent -> validateEventTransition(event, latestEvent),
                        () -> validateInitialEvent(event));
        
        govUkPayEventDao.insert(event);
        LOGGER.info("Inserted GOV.UK Pay event of type {} for payment {}", eventType, payment.getExternalId());
    }

    private void validateInitialEvent(GovUkPayEvent event) {
        if (!govUkPayEventStateGraph.isValidStartValue(event.getEventType())) {
            throw new InvalidGovUkPayEventInsertionException(event);
        }
    }

    private void validateEventTransition(GovUkPayEvent event, GovUkPayEvent latestEvent) {
        if (!govUkPayEventStateGraph.isValidTransition(latestEvent.getEventType(), event.getEventType())) {
            throw new InvalidGovUkPayEventInsertionException(event, latestEvent);
        }
    }
}
