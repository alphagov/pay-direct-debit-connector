package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_EDITED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING_WAITING_FOR_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;

public class PaymentRequestEvent {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestEvent.class);

    private Long id;
    private Long paymentRequestId;
    private Type eventType;
    private SupportedEvent event;
    private ZonedDateTime eventDate;

    public PaymentRequestEvent(Long id, Long paymentRequestId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this.id = id;
        this.paymentRequestId = paymentRequestId;
        this.eventType = eventType;
        this.event = event;
        this.eventDate = eventDate;
    }

    private PaymentRequestEvent(Long paymentRequestId, Type eventType, SupportedEvent event) {
        this(null, paymentRequestId, eventType, event, ZonedDateTime.now());
    }

    public static PaymentRequestEvent chargeCreated(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, CHARGE_CREATED);
    }

    public static PaymentRequestEvent tokenExchanged(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, TOKEN_EXCHANGED);
    }

    public static PaymentRequestEvent directDebitDetailsReceived(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public static PaymentRequestEvent payerCreated(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.PAYER, PAYER_CREATED);
    }

    public static PaymentRequestEvent payerEdited(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.PAYER, PAYER_EDITED);
    }

    public static PaymentRequestEvent directDebitDetailsConfirmed(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public static PaymentRequestEvent paymentPending(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAYMENT_PENDING);
    }

    public static PaymentRequestEvent paymentCreated(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE,
                PAYMENT_PENDING_WAITING_FOR_PROVIDER);
    }

    public static PaymentRequestEvent paymentSubmitted(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAYMENT_SUBMITTED);
    }

    public static PaymentRequestEvent paymentCancelled(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAYMENT_CANCELLED_BY_USER);
    }

    public static PaymentRequestEvent paymentFailed(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAYMENT_FAILED);
    }

    public static PaymentRequestEvent mandatePending(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.MANDATE, MANDATE_PENDING);
    }

    public static PaymentRequestEvent mandateFailed(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.MANDATE, MANDATE_FAILED);
    }

    public static PaymentRequestEvent mandateCancelled(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.MANDATE, MANDATE_CANCELLED);
    }

    public static PaymentRequestEvent mandateActive(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.MANDATE, MANDATE_ACTIVE);
    }

    public static PaymentRequestEvent paidOut(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAID_OUT);
    }

    public static PaymentRequestEvent payoutPaid(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAID);
    }

    public static PaymentRequestEvent paymentMethodChanged(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public Type getEventType() {
        return eventType;
    }

    public void setEventType(Type eventType) {
        this.eventType = eventType;
    }

    public SupportedEvent getEvent() {
        return event;
    }

    public void setEvent(SupportedEvent event) {
        this.event = event;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public enum Type {
        PAYER, CHARGE, MANDATE
    }

    public enum SupportedEvent {
        CHARGE_CREATED,
        TOKEN_EXCHANGED,
        DIRECT_DEBIT_DETAILS_RECEIVED,
        PAYER_CREATED,
        PAYER_EDITED,
        DIRECT_DEBIT_DETAILS_CONFIRMED,
        DIRECT_DEBIT_DETAILS_SUBMITTED,
        MANDATE_PENDING,
        MANDATE_ACTIVE,
        MANDATE_FAILED,
        MANDATE_CANCELLED,
        PAYMENT_PENDING_WAITING_FOR_PROVIDER,
        PAYMENT_CANCELLED_BY_USER,
        PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE,
        PAYMENT_PENDING,
        PAYMENT_SUBMITTED,
        PAYMENT_FAILED,
        PAID_OUT,
        PAID;

        public static SupportedEvent fromString(String event) throws UnsupportedPaymentRequestEventException {
            try {
                return valueOf(event);
            } catch (Exception e) {
                LOGGER.error("Tried to parse unknown event {}", event);
                throw new UnsupportedPaymentRequestEventException(event);
            }
        }
    }
}
