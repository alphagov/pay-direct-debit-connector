package uk.gov.pay.directdebit.events.model;

import uk.gov.pay.directdebit.events.service.EventActionMapper;
import uk.gov.pay.directdebit.events.service.GoCardlessEventActionMapper;
import uk.gov.pay.directdebit.events.service.GovUkEventActionMapper;

public interface Event {

    EventType getEventType();
    
    String getAction();
    
    enum EventType {
        GOCARDLESS(new GoCardlessEventActionMapper()),
        GOVUK( new GovUkEventActionMapper());

        private final EventActionMapper eventActionMapper;

        EventType (EventActionMapper eventActionMapper) {
            this.eventActionMapper = eventActionMapper;
        }

        public EventActionMapper getEventActionMapper() {
            return eventActionMapper;
        }
    }
}
