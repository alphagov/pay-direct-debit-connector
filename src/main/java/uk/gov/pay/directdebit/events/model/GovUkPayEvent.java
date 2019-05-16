package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.events.model.Event.EventType.GOVUK;

public class GovUkPayEvent implements Event {

    private Long id;
    private final String eventId;
    private final String action;
    private final ZonedDateTime createdAt;
    private final MandateExternalId mandateId;

    public GovUkPayEvent(String eventId, String action, ZonedDateTime createdAt, MandateExternalId mandateId) {
        this.eventId = eventId;
        this.action = action;
        this.createdAt = createdAt;
        this.mandateId = mandateId;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public MandateExternalId getMandateId() {
        return mandateId;
    }

    @Override
    public EventType getEventType() {
        return GOVUK;
    }

    @Override
    public String getAction() {
        return null;
    }
}
