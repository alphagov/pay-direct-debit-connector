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
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.mandatePending;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paidOut;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.payerCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.paymentPending;
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
                charge.getType(), charge.getPaymentRequestExternalId(),
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
        insertEventFor(charge, directDebitDetailsReceived(charge.getPaymentRequestId()));
    }

    public void registerDirectDebitConfirmedEventFor(Transaction charge) {
        PaymentRequestEvent event = directDebitDetailsConfirmed(charge.getPaymentRequestId());
        insertEventFor(charge, event);
    }

    public void registerPayerCreatedEventFor(Transaction charge) {
        PaymentRequestEvent event = payerCreated(charge.getPaymentRequestId());
        insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentCreatedEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentCreated(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPendingEventFor(Transaction charge) {
        PaymentRequestEvent event = paymentPending(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public PaymentRequestEvent registerPaymentPaidOutEventFor(Transaction charge) {
        PaymentRequestEvent event = paidOut(charge.getPaymentRequestId());
        return insertEventFor(charge, event);
    }

    public void registerTokenExchangedEventFor(Transaction charge) {
        PaymentRequestEvent event = tokenExchanged(charge.getPaymentRequestId());
        insertEventFor(charge, event);
    }

    public Optional<PaymentRequestEvent> findBy(Long paymentRequestId, PaymentRequestEvent.Type type, PaymentRequestEvent.SupportedEvent event) {
        return paymentRequestEventDao.findByPaymentRequestIdAndEvent(paymentRequestId, type, event);
    }

    public PaymentRequestEvent registerMandatePendingEventFor(Transaction transaction) {
        PaymentRequestEvent event = mandatePending(transaction.getPaymentRequestId());
        return insertEventFor(transaction, event);
    }

    public PaymentRequestEvent registerMandateActiveEventFor(Transaction transaction) {
        PaymentRequestEvent event = mandateActive(transaction.getPaymentRequestId());
        return insertEventFor(transaction, event);
    }
}
