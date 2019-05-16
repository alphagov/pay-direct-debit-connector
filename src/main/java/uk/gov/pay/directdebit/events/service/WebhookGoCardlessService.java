package uk.gov.pay.directdebit.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessMandateHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.GoCardlessPaymentHandler;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WebhookGoCardlessService implements AlexAndDanEventService<GoCardlessEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessEventService goCardlessService;
    private final MandateService mandateService;


    @Inject
    public WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                                    GoCardlessPaymentHandler goCardlessPaymentHandler,
                                    GoCardlessMandateHandler goCardlessMandateHandler,
                                    MandateService mandateService) {
        this.goCardlessService = goCardlessService;
        this.mandateService = mandateService;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach(goCardlessService::storeEvent);

        Set<String> idsOfMandatesAffectedByEvents = getMandateIds(events);
        Set<String> idsOfPaymentsAffectedByEvents = getPaymentIds(events);
        // Need to do payouts as well

        idsOfMandatesAffectedByEvents.forEach(mandateService::updateMandateStatus);
    }
    
    private static Set<String> getMandateIds(List<GoCardlessEvent> events) {
        return events.stream()
                .filter(goCardlessEvent -> goCardlessEvent.getResourceType() == GoCardlessResourceType.MANDATES)
                .map(GoCardlessEvent::getMandateId) // Is it safe to assume because the resource is MANDATES this won’t be null?
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> getPaymentIds(List<GoCardlessEvent> events) {
        return events.stream()
                .filter(goCardlessEvent -> goCardlessEvent.getResourceType() == GoCardlessResourceType.PAYMENTS)
                .map(GoCardlessEvent::getPaymentId) // Is this safe?
                .collect(Collectors.toUnmodifiableSet());
    }
}
