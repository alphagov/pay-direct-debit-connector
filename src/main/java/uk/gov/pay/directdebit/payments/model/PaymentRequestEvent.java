package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
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

    public static PaymentRequestEvent directDebitDetailsConfirmed(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public static PaymentRequestEvent mandateCreated(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.MANDATE, MANDATE_CREATED);
    }

    public static PaymentRequestEvent paidOut(Long paymentRequestId) {
        return new PaymentRequestEvent(paymentRequestId, Type.CHARGE, PAID_OUT);
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
        DIRECT_DEBIT_DETAILS_CONFIRMED,
        MANDATE_CREATED,
        PAID_OUT;

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
