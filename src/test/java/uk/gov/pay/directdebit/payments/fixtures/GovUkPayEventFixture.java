package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Payment;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventBuilder.aGovUkPayEvent;

public class GovUkPayEventFixture implements DbFixture<GovUkPayEventFixture, GovUkPayEvent> {
    private Long id = RandomUtils.nextLong();
    private Mandate mandate;
    private Payment payment;
    private ZonedDateTime eventDate = ZonedDateTime.parse("2019-06-07T08:46:01.123456Z");
    private GovUkPayEvent.ResourceType resourceType = GovUkPayEvent.ResourceType.MANDATE;
    private GovUkPayEvent.GovUkPayEventType eventType = GovUkPayEvent.GovUkPayEventType.MANDATE_CREATED;

    private GovUkPayEventFixture() {
    }

    @Override
    public GovUkPayEvent toEntity() {
        return aGovUkPayEvent()
                .withId(id)
                .withMandate(mandate)
                .withPayment(payment)
                .withEventDate(eventDate)
                .withResourceType(resourceType)
                .withEventType(eventType)
                .build();
    }

    @Override
    public GovUkPayEventFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h -> h.execute(
                "INSERT INTO" +
                        "   govukpay_events(" +
                        "       id," +
                        "       mandate_id," +
                        "       payment_id," +
                        "       event_date," +
                        "       resource_type," +
                        "       event_type) " +
                        "   VALUES(?, ?, ?, ?, ?, ?)",
                id,
                mandate == null ? null : mandate.getId(),
                payment == null ? null : payment.getId(),
                Timestamp.from(eventDate.toInstant()),
                resourceType.toString(),
                eventType
        ));
        return this;
    }

    public static GovUkPayEventFixture aGovUkPayEventFixture() {
        return new GovUkPayEventFixture();
    }

    public GovUkPayEventFixture withId(Long id) {
        this.id = id;
        return this;
    }
    
    public GovUkPayEventFixture withMandate(Mandate mandate) {
        this.mandate = mandate;
        return this;
    }

    public GovUkPayEventFixture withPayment(Payment payment) {
        this.payment = payment;
        return this;
    }

    public GovUkPayEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public GovUkPayEventFixture withResourceType(GovUkPayEvent.ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public GovUkPayEventFixture withEventType(GovUkPayEvent.GovUkPayEventType eventType) {
        this.eventType = eventType;
        return this;
    }
}
