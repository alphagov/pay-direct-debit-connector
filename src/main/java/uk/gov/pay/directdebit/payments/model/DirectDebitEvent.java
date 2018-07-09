package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Data
public class DirectDebitEvent {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(DirectDebitEvent.class);

    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("external_id")
    private String externalId;
    
    @JsonProperty("mandate_id")
    private Long mandateId;
    
    @JsonProperty("mandate_external_id")
    private String mandateExternalId;

    @JsonProperty("transaction_id")
    private Long transactionId;
    
    @JsonProperty("transaction_external_id")
    private String transactionExternalId;

    @JsonProperty("event_type")
    private Type eventType;

    @JsonProperty
    private SupportedEvent event;

    @JsonProperty("event_date")
    @JsonSerialize(using = CustomDateSerializer.class)
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
    
    public static DirectDebitEvent paymentExpired(Long mandateId, Long transactionId) {
        return new DirectDebitEvent(mandateId, transactionId, CHARGE, PAYMENT_EXPIRED_BY_SYSTEM);
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
