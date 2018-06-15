package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.exception.UnsupportedDirectDebitEventException;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYER_EDITED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.MANDATE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.PAYER;

public class DirectDebitEvent {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(DirectDebitEvent.class);

    private Long id;
    private Long mandateId;
    private Long transactionId;
    private Type eventType;
    private SupportedEvent event;
    private ZonedDateTime eventDate;

    public DirectDebitEvent(Long id, Long mandateId, Long transactionId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this.id = id;
        this.mandateId = mandateId;
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.event = event;
        this.eventDate = eventDate;
    }

    private DirectDebitEvent(Long mandateId, Long transactionId, Type eventType, SupportedEvent event) {
        this(null, mandateId, transactionId, eventType, event, ZonedDateTime.now());
    }
    
    public static DirectDebitEvent tokenExchanged(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, TOKEN_EXCHANGED);
    }

    public static DirectDebitEvent directDebitDetailsReceived(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public static DirectDebitEvent payerCreated(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, PAYER, PAYER_CREATED);
    }

    public static DirectDebitEvent payerEdited(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, PAYER, PAYER_EDITED);
    }

    public static DirectDebitEvent directDebitDetailsConfirmed(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }
    
    public static DirectDebitEvent mandatePending(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_PENDING);
    }

    public static DirectDebitEvent mandateFailed(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_FAILED);
    }

    public static DirectDebitEvent mandateCancelled(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_CANCELLED);
    }
    
    public static DirectDebitEvent mandateActive(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_ACTIVE);
    }

    public static DirectDebitEvent awaitingDirectDebitDetails(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, TOKEN_EXCHANGED);
    }
    
    public static DirectDebitEvent paymentMethodChanged(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    public static DirectDebitEvent chargeCreated(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, CHARGE_CREATED);
    }
    
    public static DirectDebitEvent paymentAcknowledged(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE,
                PAYMENT_ACKNOWLEDGED_BY_PROVIDER);
    }

    public static DirectDebitEvent paymentSubmittedToProvider(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE,
                PAYMENT_SUBMITTED_TO_PROVIDER);
    }

    public static DirectDebitEvent paymentSubmitted(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }

    public static DirectDebitEvent paymentCancelled(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAYMENT_CANCELLED_BY_USER);
    }

    public static DirectDebitEvent paymentFailed(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAYMENT_FAILED);
    }
    public static DirectDebitEvent paidOut(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAID_OUT);
    }

    public static DirectDebitEvent payoutPaid(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAID);
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

        public static SupportedEvent fromString(String event) throws UnsupportedDirectDebitEventException {
            try {
                return valueOf(event);
            } catch (Exception e) {
                LOGGER.error("Tried to parse unknown event {}", event);
                throw new UnsupportedDirectDebitEventException(event);
            }
        }
    }
}
