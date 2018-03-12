package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.CHARGE_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.CHARGE;

public class PaymentRequestEventFixture implements DbFixture<PaymentRequestEventFixture, PaymentRequestEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private Type eventType = CHARGE;
    private SupportedEvent event = CHARGE_CREATED;
    private ZonedDateTime eventDate = ZonedDateTime.now(ZoneOffset.UTC);

    private PaymentRequestEventFixture() {
    }

    public static PaymentRequestEventFixture aPaymentRequestEventFixture() {
        return new PaymentRequestEventFixture();
    }

    public Long getId() {
        return id;
    }

    public PaymentRequestEventFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public PaymentRequestEventFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public Type getEventType() {
        return eventType;
    }

    public PaymentRequestEventFixture withEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public SupportedEvent getEvent() {
        return event;
    }

    public PaymentRequestEventFixture withEvent(SupportedEvent event) {
        this.event = event;
        return this;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public PaymentRequestEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    @Override
    public PaymentRequestEventFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    payment_request_events(\n" +
                                "        id,\n" +
                                "        payment_request_id,\n" +
                                "        event_type,\n" +
                                "        event,\n" +
                                "        event_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?)\n",
                        id,
                        paymentRequestId,
                        eventType.toString(),
                        event.toString(),
                        Timestamp.from(eventDate.toInstant())
                )
        );
        return this;
    }

    @Override
    public PaymentRequestEvent toEntity() {
        return new PaymentRequestEvent(id, paymentRequestId, eventType, event, eventDate);
    }

}
