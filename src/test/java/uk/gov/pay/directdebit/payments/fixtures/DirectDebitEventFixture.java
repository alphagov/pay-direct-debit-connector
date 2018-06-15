package uk.gov.pay.directdebit.payments.fixtures;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;

public class DirectDebitEventFixture implements DbFixture<DirectDebitEventFixture, DirectDebitEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
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

    public Long getId() {
        return id;
    }

    public DirectDebitEventFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public DirectDebitEventFixture withTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public DirectDebitEventFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }
    
    public Type getEventType() {
        return eventType;
    }

    public DirectDebitEventFixture withEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public SupportedEvent getEvent() {
        return event;
    }

    public DirectDebitEventFixture withEvent(SupportedEvent event) {
        this.event = event;
        return this;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
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
                                "        mandate_id,\n" +
                                "        transaction_id,\n" +
                                "        event_type,\n" +
                                "        event,\n" +
                                "        event_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?)\n",
                        id,
                        mandateId,
                        transactionId,
                        eventType.toString(),
                        event.toString(),
                        Timestamp.from(eventDate.toInstant())
                )
        );
        return this;
    }

    @Override
    public DirectDebitEvent toEntity() {
        return new DirectDebitEvent(id, mandateId, transactionId, eventType, event, eventDate);
    }

}
