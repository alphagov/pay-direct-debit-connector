package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PaymentRequestEventFixture implements DbFixture<PaymentRequestEventFixture, PaymentRequestEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private PaymentRequestEvent.Type eventType;
    private PaymentRequestEvent.SupportedEvent event;
    private ZonedDateTime eventDate = ZonedDateTime.now(ZoneId.of("UTC"));

    private PaymentRequestEventFixture() {
    }

    public static PaymentRequestEventFixture aPaymentRequestEventFixture() {
        return new PaymentRequestEventFixture();
    }

    public PaymentRequestEventFixture withId(long id) {
        this.id = id;
        return this;
    }
    public PaymentRequestEventFixture withPaymentRequestId(long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }
    public PaymentRequestEventFixture withType(PaymentRequestEvent.Type type) {
        this.eventType = type;
        return this;
    }

    public PaymentRequestEventFixture withEvent(PaymentRequestEvent.SupportedEvent event) {
        this.event = event;
        return this;
    }

    public PaymentRequestEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public PaymentRequestEvent.Type getEventType() {
        return eventType;
    }

    public PaymentRequestEvent.SupportedEvent getEvent() {
        return event;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    @Override
    public PaymentRequestEventFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    payment_request(\n" +
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
