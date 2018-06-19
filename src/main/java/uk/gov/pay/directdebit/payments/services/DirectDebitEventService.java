package uk.gov.pay.directdebit.payments.services;

import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.DirectDebitEventDao;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.awaitingDirectDebitDetails;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.directDebitDetailsConfirmed;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.directDebitDetailsReceived;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.mandateActive;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.mandateCancelled;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.mandateFailed;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.mandatePending;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paidOut;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.payerCreated;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.payerEdited;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentAcknowledged;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentCancelled;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentFailed;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentMethodChanged;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentSubmitted;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.paymentSubmittedToProvider;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.payoutPaid;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.tokenExchanged;

public class DirectDebitEventService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(DirectDebitEventService.class);

    private final DirectDebitEventDao directDebitEventDao;

    @Inject
    public DirectDebitEventService(DirectDebitEventDao directDebitEventDao) {
        this.directDebitEventDao = directDebitEventDao;
    }

    private DirectDebitEvent insertEventFor(Transaction charge, DirectDebitEvent directDebitEvent) {
        LOGGER.info("Creating event for transaction {}: {} - {}", 
                charge.getExternalId(),
                directDebitEvent.getEventType(), 
                directDebitEvent.getEvent());
        Long id = directDebitEventDao.insert(directDebitEvent);
        directDebitEvent.setId(id);
        return directDebitEvent;
    }

    public DirectDebitEvent insertEventFor(Mandate mandate, DirectDebitEvent directDebitEvent) {
        LOGGER.info("Creating event for mandate {}: {} - {}",
                mandate.getExternalId(), directDebitEvent.getEventType(), directDebitEvent.getEvent());
        Long id = directDebitEventDao.insert(directDebitEvent);
        directDebitEvent.setId(id);
        return directDebitEvent;
    }

    public void registerTokenExchangedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = tokenExchanged(mandate.getId());
        insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentSubmittedToProviderEventFor(Transaction transaction) {
        DirectDebitEvent directDebitEvent = paymentSubmittedToProvider(transaction.getMandate().getId(), transaction.getId());
        return insertEventFor(transaction.getMandate(), directDebitEvent);
    }

    public DirectDebitEvent registerPaymentCancelledEventFor(Mandate mandate, Transaction transaction) {
        DirectDebitEvent directDebitEvent = paymentCancelled(mandate.getId(), transaction.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerMandateFailedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateFailed(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerMandateCancelledEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateCancelled(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentFailedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        DirectDebitEvent directDebitEvent = paymentFailed(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentAcknowledgedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        DirectDebitEvent directDebitEvent = paymentAcknowledged(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentSubmittedEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        DirectDebitEvent directDebitEvent = paymentSubmitted(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentPaidOutEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        DirectDebitEvent directDebitEvent = paidOut(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, directDebitEvent);
    }

    public DirectDebitEvent registerPayoutPaidEventFor(Transaction transaction) {
        Mandate mandate = transaction.getMandate();
        DirectDebitEvent directDebitEvent = payoutPaid(mandate.getId(), transaction.getId());
        return insertEventFor(transaction, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentMethodChangedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = paymentMethodChanged(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }
    
    public DirectDebitEvent registerMandatePendingEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandatePending(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerMandateActiveEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateActive(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerAwaitingDirectDebitDetailsEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = awaitingDirectDebitDetails(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }
    
    public void registerPayerCreatedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = payerCreated(mandate.getId());
        insertEventFor(mandate, directDebitEvent);
    }

    public void registerDirectDebitConfirmedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = directDebitDetailsConfirmed(mandate.getId());
        insertEventFor(mandate, directDebitEvent);
    }

    public void registerDirectDebitReceivedEventFor(Mandate mandate) {
        insertEventFor(mandate, directDebitDetailsReceived(mandate.getId()));
    }

    public DirectDebitEvent registerPayerEditedEventFor(Mandate mandate) {
        return insertEventFor(mandate, payerEdited(mandate.getId()));
    }
    
    public Optional<DirectDebitEvent> findBy(Long mandateId, DirectDebitEvent.Type type, DirectDebitEvent.SupportedEvent event) {
        return directDebitEventDao.findByMandateIdAndEvent(mandateId, type, event);
    }
}
