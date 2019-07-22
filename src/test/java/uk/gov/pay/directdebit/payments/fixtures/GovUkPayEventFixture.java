package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventBuilder.aGovUkPayEvent;

public class GovUkPayEventFixture implements DbFixture<GovUkPayEventFixture, GovUkPayEvent> {
    private Long id = RandomUtils.nextLong();
    private Long mandateId;
    private Long paymentId;
    private ZonedDateTime eventDate = ZonedDateTime.parse("2019-06-07T08:46:01.123456Z");
    private GovUkPayEvent.ResourceType resourceType = GovUkPayEvent.ResourceType.MANDATE;
    private GovUkPayEventType eventType = GovUkPayEventType.MANDATE_CREATED;

    private GovUkPayEventFixture() {
    }

    @Override
    public GovUkPayEvent toEntity() {
        return aGovUkPayEvent()
                .withId(id)
                .withMandateId(mandateId)
                .withPaymentId(paymentId)
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
                mandateId,
                paymentId,
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
    
    public GovUkPayEventFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public GovUkPayEventFixture withPaymentId(Long paymentId) {
        this.paymentId = paymentId;
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

    public GovUkPayEventFixture withEventType(GovUkPayEventType eventType) {
        this.eventType = eventType;
        return this;
    }
}
