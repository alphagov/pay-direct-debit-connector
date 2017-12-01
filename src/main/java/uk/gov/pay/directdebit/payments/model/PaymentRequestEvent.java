package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import java.time.ZonedDateTime;

public class PaymentRequestEvent {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRequestEvent.class);

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
        CHARGE, MANDATE, PAYER;
    }

    public enum SupportedEvent {
        CHARGE_CREATED,
        SHOW_ENTER_DIRECT_DEBIT_DETAILS,
        SYSTEM_CANCEL,
        PAYMENT_EXPIRED,
        SHOW_CONFIRM,
        CREATE_CUSTOMER_FAILED,
        CREATE_CUSTOMER_BANK_ACCOUNT_ERROR,
        USER_CANCEL,
        MANDATE_PAYMENT_CREATED,
        CREATE_MANDATE_FAILED,
        CREATE_MANDATE_ERROR,
        WEBHOOK_ACTION_PAYMENT_CREATED,
        WEBHOOK_ACTION_CUSTOMER_DENIED,
        WEBHOOK_ACTION_PAYMENT_ERROR,
        WEBHOOK_ACTION_CONFIRMED,
        WEBHOOK_ACTION_DATA_MISSING,
        WEBHOOK_ACTION_CUSTOMER_CANCELLED,
        WEBHOOK_ACTION_PAID_OUT;


        public static SupportedEvent fromString(String event) throws UnsupportedPaymentRequestEventException {
            try {
                return SupportedEvent.valueOf(event);
            } catch (Exception e) {
                logger.warn("Tried to parse unknown event {}", event);
                throw new UnsupportedPaymentRequestEventException(event);
            }
        }
    }
}
