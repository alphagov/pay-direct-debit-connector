package uk.gov.pay.directdebit.payments.model;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.exception.UnsupportedDirectDebitEventException;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYER_EDITED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_ACKNOWLEDGED_BY_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_EXPIRED_BY_SYSTEM;
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
    private String externalId;
    private Long mandateId;
    private String mandateExternalId;
    private Long transactionId;
    private String transactionExternalId;
    private Type eventType;
    private SupportedEvent event;
    private ZonedDateTime eventDate;

    public DirectDebitEvent(){};
    
    public DirectDebitEvent(Long id, String externalId, Long mandateId, Long transactionId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this.id = id;
        this.externalId = externalId;
        this.mandateId = mandateId;
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.event = event;
        this.eventDate = eventDate;
    }

    public DirectDebitEvent(Long id, String externalId, Long mandateId, String mandateExternalId, Long transactionId, String transactionExternalId, Type eventType, SupportedEvent event, ZonedDateTime eventDate) {
        this.id = id;
        this.externalId = externalId;
        this.mandateId = mandateId;
        this.mandateExternalId = mandateExternalId;
        this.transactionExternalId = transactionExternalId;
        this.eventType = eventType;
        this.transactionId = transactionId;
        this.event = event;
        this.eventDate = eventDate;
    }

    private DirectDebitEvent(Long mandateId, Long transactionId, Type eventType, SupportedEvent event) {
        this(null, RandomIdGenerator.newId(), mandateId, transactionId, eventType, event, ZonedDateTime.now());
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

    public static DirectDebitEvent mandateCreated(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_CREATED);
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
    
    public static DirectDebitEvent mandateExpiredBySystem(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_EXPIRED_BY_SYSTEM);
    }
    
    public static DirectDebitEvent mandateActive(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, MANDATE_ACTIVE);
    }
    
    public static DirectDebitEvent paymentMethodChanged(Long mandateId) {
        return new DirectDebitEvent(mandateId, null, MANDATE, PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    public static DirectDebitEvent transactionCreated(Long mandateId, Long transactionId) {
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
    
    public static DirectDebitEvent paymentExpired(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAYMENT_EXPIRED_BY_SYSTEM);
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public String getMandateExternalId() {
        return mandateExternalId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getTransactionExternalId() {
        return transactionExternalId;
    }

    public Type getEventType() {
        return eventType;
    }

    public SupportedEvent getEvent() {
        return event;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setMandateId(Long mandateId) {
        this.mandateId = mandateId;
    }

    public void setMandateExternalId(String mandateExternalId) {
        this.mandateExternalId = mandateExternalId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public void setTransactionExternalId(String transactionExternalId) {
        this.transactionExternalId = transactionExternalId;
    }

    public void setEventType(Type eventType) {
        this.eventType = eventType;
    }

    public void setEvent(SupportedEvent event) {
        this.event = event;
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
        MANDATE_CREATED,
        MANDATE_PENDING,
        MANDATE_ACTIVE,
        MANDATE_FAILED,
        MANDATE_CANCELLED,
        MANDATE_EXPIRED_BY_SYSTEM,
        PAYMENT_SUBMITTED_TO_PROVIDER,
        PAYMENT_CANCELLED_BY_USER,
        PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE,
        PAYMENT_EXPIRED_BY_SYSTEM,
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
