package uk.gov.pay.directdebit.events.model;

import java.time.ZonedDateTime;

public interface Event {
    ZonedDateTime getTimestamp();
}
