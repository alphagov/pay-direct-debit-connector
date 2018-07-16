package uk.gov.pay.directdebit.payments.fixtures;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;


public class DirectDebitEventFixture implements DbFixture<DirectDebitEventFixture, DirectDebitEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private Long mandateId = RandomUtils.nextLong(1, 99999);
    private Long transactionId = null;
    private Type eventType = CHARGE;
    private SupportedEvent event = CHARGE_CREATED;
    private ZonedDateTime eventDate = ZonedDateTime.now(ZoneOffset.UTC);

    private DirectDebitEventFixture() {
    }

    public static DirectDebitEventFixture aDirectDebitEventFixture() {
        return new DirectDebitEventFixture();
    }

    public DirectDebitEventFixture withId(Long id) {
        this.id = id;
        return this;
    }
    
    public DirectDebitEventFixture withExternalId(String externalId){
        this.externalId = externalId;
        return this;
    }

    public DirectDebitEventFixture withTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public DirectDebitEventFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public DirectDebitEventFixture withEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public DirectDebitEventFixture withEvent(SupportedEvent event) {
        this.event = event;
        return this;
    }

    public DirectDebitEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    @Override
    public DirectDebitEventFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    events(\n" +
                                "        id,\n" +
                                "        external_id,\n" +
                                "        mandate_id,\n" +
                                "        transaction_id,\n" +
                                "        event_type,\n" +
                                "        event,\n" +
                                "        event_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?)\n",
                        getId(),
                        getExternalId(),
                        getMandateId(),
                        getTransactionId(),
                        getEventType().toString(),
                        getEvent().toString(),
                        Timestamp.from(getEventDate().toInstant())
                )
        );
        return this;
    }

    @Override
    public DirectDebitEvent toEntity() {
        return new DirectDebitEvent(getId(), getExternalId(), getMandateId(), getTransactionId(), getEventType(), getEvent(), getEventDate());
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

    public Long getTransactionId() {
        return transactionId;
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
}
