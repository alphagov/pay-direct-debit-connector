package uk.gov.pay.directdebit.payments.services;

import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.dao.DirectDebitEventDao;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.Payment;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.*;

public class DirectDebitEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectDebitEventService.class);

    private final DirectDebitEventDao directDebitEventDao;

    @Inject
    public DirectDebitEventService(DirectDebitEventDao directDebitEventDao) {
        this.directDebitEventDao = directDebitEventDao;
    }

    private DirectDebitEvent insertEventFor(Payment charge, DirectDebitEvent directDebitEvent) {
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
    
    public DirectDebitEvent registerPaymentExpiredEventFor(Payment payment) {
        DirectDebitEvent directDebitEvent = paymentExpired(payment.getMandate().getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
    }

    public DirectDebitEvent registerTransactionCreatedEventFor(Payment payment) {
        DirectDebitEvent directDebitEvent = transactionCreated(payment.getMandate().getId(), payment.getId());
        return insertEventFor(payment.getMandate(), directDebitEvent);
    }
    
    public DirectDebitEvent registerPaymentSubmittedToProviderEventFor(Payment payment) {
        DirectDebitEvent directDebitEvent = paymentSubmittedToProvider(payment.getMandate().getId(), payment.getId());
        return insertEventFor(payment.getMandate(), directDebitEvent);
    }

    public DirectDebitEvent registerPaymentCancelledEventFor(Mandate mandate, Payment payment) {
        DirectDebitEvent directDebitEvent = paymentCancelled(mandate.getId(), payment.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public void registerMandateCreatedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateCreated(mandate.getId());
        insertEventFor(mandate, directDebitEvent);
    }
    
    public DirectDebitEvent registerMandateFailedEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateFailed(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public DirectDebitEvent registerMandateCancelledEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateCancelled(mandate.getId());
        return insertEventFor(mandate, directDebitEvent);
    }

    public void registerMandateExpiredEventFor(Mandate mandate) {
        DirectDebitEvent directDebitEvent = mandateExpiredBySystem(mandate.getId());
        insertEventFor(mandate, directDebitEvent);
    }


    public DirectDebitEvent registerPaymentFailedEventFor(Payment payment) {
        Mandate mandate = payment.getMandate();
        DirectDebitEvent directDebitEvent = paymentFailed(mandate.getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentAcknowledgedEventFor(Payment payment) {
        Mandate mandate = payment.getMandate();
        DirectDebitEvent directDebitEvent = paymentAcknowledged(mandate.getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentSubmittedEventFor(Payment payment) {
        Mandate mandate = payment.getMandate();
        DirectDebitEvent directDebitEvent = paymentSubmitted(mandate.getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
    }

    public DirectDebitEvent registerPaymentPaidOutEventFor(Payment payment) {
        Mandate mandate = payment.getMandate();
        DirectDebitEvent directDebitEvent = paidOut(mandate.getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
    }

    public DirectDebitEvent registerPayoutPaidEventFor(Payment payment) {
        Mandate mandate = payment.getMandate();
        DirectDebitEvent directDebitEvent = payoutPaid(mandate.getId(), payment.getId());
        return insertEventFor(payment, directDebitEvent);
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

    public void registerPayerEditedEventFor(Mandate mandate) {
        insertEventFor(mandate, payerEdited(mandate.getId()));
    }

    public Optional<DirectDebitEvent> findBy(Long mandateId, DirectDebitEvent.Type type, DirectDebitEvent.SupportedEvent event) {
        return directDebitEventDao.findByMandateIdAndEvent(mandateId, type, event);
    }
}
