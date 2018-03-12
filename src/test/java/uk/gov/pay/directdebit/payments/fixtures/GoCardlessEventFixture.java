package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GoCardlessEventFixture implements DbFixture<GoCardlessEventFixture, GoCardlessEvent> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestEventsId = RandomUtils.nextLong(1, 99999);
    private String eventId = RandomIdGenerator.newId();
    private String action = RandomStringUtils.randomAlphabetic(20);
    private GoCardlessResourceType resourceType = GoCardlessResourceType.PAYMENTS;
    private String resourceId = RandomStringUtils.randomAlphabetic(20);
    private String json = "{\"id\": \"somejson\"}";
    private ZonedDateTime createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    private GoCardlessEventFixture() {
    }

    public static GoCardlessEventFixture aGoCardlessEventFixture() {
        return new GoCardlessEventFixture();
    }

    public Long getId() {
        return id;
    }

    public GoCardlessEventFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public Long getPaymentRequestEventsId() {
        return paymentRequestEventsId;
    }

    public GoCardlessEventFixture withPaymentRequestEventsId(Long paymentRequestEventsId) {
        this.paymentRequestEventsId = paymentRequestEventsId;
        return this;
    }

    public String getJson() {
        return json;
    }

    public GoCardlessEventFixture withJson(String json) {
        this.json = json;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public GoCardlessEventFixture withEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getAction() {
        return action;
    }

    public GoCardlessEventFixture withAction(String action) {
        this.action = action;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public GoCardlessEventFixture withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public GoCardlessResourceType getResourceType() {
        return resourceType;
    }

    public GoCardlessEventFixture withResourceType(GoCardlessResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public GoCardlessEventFixture withCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public GoCardlessEventFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    gocardless_events(\n" +
                                "        id,\n" +
                                "        payment_request_events_id,\n" +
                                "        event_id,\n" +
                                "        action,\n" +
                                "        resource_type,\n" +
                                "        json,\n" +
                                "        created_at\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        paymentRequestEventsId,
                        eventId,
                        action,
                        resourceType.toString(),
                        json,
                        Timestamp.from(createdAt.toInstant())
                )
        );
        return this;
    }

    @Override
    public GoCardlessEvent toEntity() {
        return new GoCardlessEvent(id, paymentRequestEventsId, eventId, action, resourceType, json, createdAt);
    }

}
