package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import java.time.ZonedDateTime;

public class PaymentRequestEvent {
    private static final Logger logger = PayLoggerFactory.getLogger(PaymentRequestEvent.class);

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

    public PaymentRequestEvent(Long paymentRequestId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this(null, paymentRequestId, eventType, event, eventDate);
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
        CHARGE
    }

    public enum SupportedEvent {
        CHARGE_CREATED,
        TOKEN_EXCHANGED,
        DIRECT_DEBIT_DETAILS_RECEIVED,
        PAYER_CREATED;

        public static SupportedEvent fromString(String event) throws UnsupportedPaymentRequestEventException {
            try {
                return SupportedEvent.valueOf(event);
            } catch (Exception e) {
                logger.error("Tried to parse unknown event {}", event);
                throw new UnsupportedPaymentRequestEventException(event);
            }
        }
    }
}
