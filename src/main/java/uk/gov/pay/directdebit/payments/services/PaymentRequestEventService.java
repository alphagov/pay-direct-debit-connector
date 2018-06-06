package uk.gov.pay.directdebit.payments.services;

import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.EventDao;
import uk.gov.pay.directdebit.payments.model.Event;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static uk.gov.pay.directdebit.payments.model.Event.awaitingDirectDebitDetails;
import static uk.gov.pay.directdebit.payments.model.Event.directDebitDetailsConfirmed;
import static uk.gov.pay.directdebit.payments.model.Event.directDebitDetailsReceived;
import static uk.gov.pay.directdebit.payments.model.Event.mandateActive;
import static uk.gov.pay.directdebit.payments.model.Event.mandateCancelled;
import static uk.gov.pay.directdebit.payments.model.Event.mandateFailed;
import static uk.gov.pay.directdebit.payments.model.Event.mandatePending;
import static uk.gov.pay.directdebit.payments.model.Event.paidOut;
import static uk.gov.pay.directdebit.payments.model.Event.payerCreated;
import static uk.gov.pay.directdebit.payments.model.Event.payerEdited;
import static uk.gov.pay.directdebit.payments.model.Event.paymentAcknowledged;
import static uk.gov.pay.directdebit.payments.model.Event.paymentCancelled;
import static uk.gov.pay.directdebit.payments.model.Event.paymentFailed;
import static uk.gov.pay.directdebit.payments.model.Event.paymentMethodChanged;
import static uk.gov.pay.directdebit.payments.model.Event.paymentSubmitted;
import static uk.gov.pay.directdebit.payments.model.Event.paymentSubmittedToProvider;
import static uk.gov.pay.directdebit.payments.model.Event.payoutPaid;
import static uk.gov.pay.directdebit.payments.model.Event.tokenExchanged;

public class PaymentRequestEventService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestEventService.class);

    private final EventDao eventDao;

    @Inject
    public PaymentRequestEventService(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    private Event insertEventFor(Transaction charge, Event event) {
        LOGGER.info("Creating event for transaction {}: {} - {}", 
                charge.getExternalId(),
                event.getEventType(), 
                event.getEvent());
        Long id = eventDao.insert(event);
        event.setId(id);
        return event;
    }

    public Event insertEventFor(Mandate mandate, Event event) {
        LOGGER.info("Creating event for mandate {}: {} - {}",
                mandate.getExternalId(), event.getEventType(), event.getEvent());
        Long id = eventDao.insert(event);
        event.setId(id);
        return event;
    }

    public void registerTokenExchangedEventFor(Mandate mandate) {
        Event event = tokenExchanged(mandate.getId());
        insertEventFor(mandate, event);
    }

    public Event registerPaymentSubmittedToProviderEventFor(Transaction transaction) {
        Event event = paymentSubmittedToProvider(transaction.getMandate().getId(), transaction.getId());
        return insertEventFor(transaction.getMandate(), event);
    }

    public Event registerPaymentCancelledEventFor(Mandate mandate, Transaction transaction) {
        Event event = paymentCancelled(mandate.getId(), transaction.getId());
        return insertEventFor(mandate, event);
    }

    public Event registerMandateFailedEventFor(Mandate mandate) {
        Event event = mandateFailed(mandate.getId());
        return insertEventFor(mandate, event);
    }

    public Event registerMandateCancelledEventFor(Mandate mandate) {
        Event event = mandateCancelled(mandate.getId());
        return insertEventFor(mandate, event);
    }

    public Event registerPaymentFailedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        Event event = paymentFailed(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, event);
    }

    public Event registerPaymentAcknowledgedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        Event event = paymentAcknowledged(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, event);
    }

    public Event registerPaymentSubmittedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        Event event = paymentSubmitted(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, event);
    }

    public Event registerPaymentPaidOutEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        Event event = paidOut(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, event);
    }

    public Event registerPayoutPaidEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        Event event = payoutPaid(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, event);
    }

    public Event registerPaymentMethodChangedEventFor(Mandate mandate) {
        Event event = paymentMethodChanged(mandate.getId());
        return insertEventFor(mandate, event);
    }
    
    public Event registerMandatePendingEventFor(Mandate mandate) {
        Event event = mandatePending(mandate.getId());
        return insertEventFor(mandate, event);
    }

    public Event registerMandateActiveEventFor(Mandate mandate) {
        Event event = mandateActive(mandate.getId());
        return insertEventFor(mandate, event);
    }

    public Event registerAwaitingDirectDebitDetailsEventFor(Mandate mandate) {
        Event event = awaitingDirectDebitDetails(mandate.getId());
        return insertEventFor(mandate, event);
    }
    
    public void registerPayerCreatedEventFor(Mandate mandate) {
        Event event = payerCreated(mandate.getId());
        insertEventFor(mandate, event);
    }

    public void registerDirectDebitConfirmedEventFor(Mandate mandate) {
        Event event = directDebitDetailsConfirmed(mandate.getId());
        insertEventFor(mandate, event);
    }

    public void registerDirectDebitReceivedEventFor(Mandate mandate) {
        insertEventFor(mandate, directDebitDetailsReceived(mandate.getId()));
    }

    public Event registerPayerEditedEventFor(Mandate mandate) {
        return insertEventFor(mandate, payerEdited(mandate.getId()));
    }
    
    public Optional<Event> findBy(Long mandateId, Event.Type type, Event.SupportedEvent event) {
        return eventDao.findByMandateIdAndEvent(mandateId, type, event);
    }
}
