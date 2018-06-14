package uk.gov.pay.directdebit.payments.fixtures;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.payments.model.Event;

import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.Event.Type;
import static uk.gov.pay.directdebit.payments.model.Event.Type.CHARGE;

public class EventFixture implements DbFixture<EventFixture, Event> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long mandateId = RandomUtils.nextLong(1, 99999);
    private Long transactionId = null;
    private Type eventType = CHARGE;
    private SupportedEvent event = CHARGE_CREATED;
    private ZonedDateTime eventDate = ZonedDateTime.now(ZoneOffset.UTC);

    private EventFixture() {
    }

    public static EventFixture aPaymentRequestEventFixture() {
        return new EventFixture();
    }

    public Long getId() {
        return id;
    }

    public EventFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public EventFixture withTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public EventFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }
    
    public Type getEventType() {
        return eventType;
    }

    public EventFixture withEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public SupportedEvent getEvent() {
        return event;
    }

    public EventFixture withEvent(SupportedEvent event) {
        this.event = event;
        return this;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public EventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    @Override
    public EventFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    payment_request_events(\n" +
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
    public Event toEntity() {
        return new Event(id, mandateId, transactionId, eventType, event, eventDate);
    }

}
