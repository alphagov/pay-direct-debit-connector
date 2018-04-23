package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.directDebitDetailsConfirmed;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.directDebitDetailsReceived;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.mandateActive;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.mandateCancelled;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.mandateFailed;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.mandatePending;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paidOut;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.payerCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentCancelled;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentFailed;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentMethodChanged;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentPending;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentSubmitted;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.payoutPaid;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.tokenExchanged;

public class PaymentRequestEventService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestEventService.class);

    private final PaymentRequestEventDao paymentRequestEventDao;

    @Inject
    public PaymentRequestEventService(PaymentRequestEventDao paymentRequestEventDao) {
        this.paymentRequestEventDao = paymentRequestEventDao;
    }

    private PaymentRequestEvent insertEventFor(Transaction charge, PaymentRequestEvent event) {
        LOGGER.info("Creating event for {} {}: {} - {}",
                charge.getType(), charge.getPaymentRequest().getExternalId(),
                event.getEventType(), event.getEvent());
        Long id = paymentRequestEventDao.insert(event);
        event.setId(id);
        return event;
    }

    void insertEventFor(PaymentRequest paymentRequest, PaymentRequestEvent event) {
        LOGGER.info("Creating event for payment request {}: {} - {}",
                paymentRequest.getExternalId(), event.getEventType(), event.getEvent());
        paymentRequestEventDao.insert(event);
    }

    public void registerDirectDebitReceivedEventFor(Transaction charge) {
        insertEventFor(charge, directDebitDetailsReceived(charge.getPaymentRequest().getId()));
    }

    public void registerDirectDebitConfirmedEventFor(Transaction charge) {
        PaymentRequestEvent event = directDebitDetailsConfirmed(charge.getPaymentRequest().getId());
        insertEventFor(charge, event);
    }

    public void registerPayerCreatedEventFor(Transaction charge) {
        PaymentRequestEvent event = payerCreated(charge.getPaymentRequest().getId());
        insertEventFor(charge, event);
    }

    public void registerTokenExchangedEventFor(Transaction charge) {
        PaymentRequestEvent event = tokenExchanged(charge.getPaymentRequest().getId());
        insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentCreatedEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentCreated(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentCancelledEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentCancelled(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerMandateFailedEventFor(Transaction charge) {
        PaymentRequestEvent event = mandateFailed(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerMandateCancelledEventFor(Transaction charge) {
        PaymentRequestEvent event = mandateCancelled(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentFailedEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentFailed(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPendingEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentPending(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentSubmittedEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentSubmitted(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPaidOutEventFor(Transaction charge) {
        PaymentRequestEvent event = paidOut(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPayoutPaidEventFor(Transaction charge) {
        PaymentRequestEvent event = payoutPaid(charge.getPaymentRequest().getId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerMandatePendingEventFor(Transaction transaction) {
        PaymentRequestEvent event = mandatePending(transaction.getPaymentRequest().getId());
        return insertEventFor(transaction, event);
    }

    public PaymentRequestEvent registerMandateActiveEventFor(Transaction transaction) {
        PaymentRequestEvent event = mandateActive(transaction.getPaymentRequest().getId());
        return insertEventFor(transaction, event);
    }

    public Optional<PaymentRequestEvent> findBy(Long paymentRequestId, PaymentRequestEvent.Type type, PaymentRequestEvent.SupportedEvent event) {
        return paymentRequestEventDao.findByPaymentRequestIdAndEvent(paymentRequestId, type, event);
    }

    public PaymentRequestEvent registerPaymentMethodChangedEventFor(Transaction transaction) {
        PaymentRequestEvent event = paymentMethodChanged(transaction.getPaymentRequest().getId());
        return insertEventFor(transaction, event);
    }
}
