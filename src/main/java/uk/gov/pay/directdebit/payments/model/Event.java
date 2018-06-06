package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedPaymentRequestEventException;

import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYER_EDITED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.TOKEN_EXCHANGED;

public class Event {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(Event.class);

    private Long id;
    private Long mandateId;
    private Long transactionId;
    private Type eventType;
    private SupportedEvent event;
    private ZonedDateTime eventDate;

    public Event(Long id, Long mandateId, Long transactionId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this.id = id;
        this.mandateId = mandateId;
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.event = event;
        this.eventDate = eventDate;
    }

    private Event(Long mandateId, Long transactionId, Type eventType, SupportedEvent event) {
        this(null, mandateId, transactionId, eventType, event, ZonedDateTime.now());
    }
    
    public static Event tokenExchanged(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, TOKEN_EXCHANGED);
    }

    public static Event directDebitDetailsReceived(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public static Event payerCreated(Long mandateId) {
        return new Event(mandateId, null, Type.PAYER, PAYER_CREATED);
    }

    public static Event payerEdited(Long mandateId) {
        return new Event(mandateId, null, Type.PAYER, PAYER_EDITED);
    }

    public static Event directDebitDetailsConfirmed(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }
    
    public static Event mandatePending(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, MANDATE_PENDING);
    }

    public static Event mandateFailed(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, MANDATE_FAILED);
    }

    public static Event mandateCancelled(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, MANDATE_CANCELLED);
    }
    
    public static Event mandateActive(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, MANDATE_ACTIVE);
    }

    public static Event awaitingDirectDebitDetails(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, TOKEN_EXCHANGED);
    }
    
    public static Event paymentMethodChanged(Long mandateId) {
        return new Event(mandateId, null, Type.MANDATE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    public static Event chargeCreated(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, CHARGE_CREATED);
    }
    
    public static Event paymentAcknowledged(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE,
                PAYMENT_ACKNOWLEDGED_BY_PROVIDER);
    }

    public static Event paymentSubmittedToProvider(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE,
                PAYMENT_SUBMITTED_TO_PROVIDER);
    }

    public static Event paymentSubmitted(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }

    public static Event paymentCancelled(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, PAYMENT_CANCELLED_BY_USER);
    }

    public static Event paymentFailed(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, PAYMENT_FAILED);
    }
    public static Event paidOut(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, PAID_OUT);
    }

    public static Event payoutPaid(Long mandateId, Long paymentRequestId) {
        return new Event(mandateId, paymentRequestId, Type.CHARGE, PAID);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public void setMandateId(Long mandateId) {
        this.mandateId = mandateId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
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
        MANDATE_PENDING,
        MANDATE_ACTIVE,
        MANDATE_FAILED,
        MANDATE_CANCELLED,
        PAYMENT_SUBMITTED_TO_PROVIDER,
        PAYMENT_CANCELLED_BY_USER,
        PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE,
        PAYMENT_ACKNOWLEDGED_BY_PROVIDER,
        PAYMENT_SUBMITTED_TO_BANK,
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
