package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.events.model.Event;

import java.util.List;

public interface AlexAndDanEventService<T extends Event> {

    void handleEvents(List<T> events);
}
