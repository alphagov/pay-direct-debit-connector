package uk.gov.pay.directdebit.events.service;

import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.MandateService;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GovUkPayEventService implements AlexAndDanEventService<GovUkPayEvent> {
    
    private final GovUkPayEventDao govUkPayEventDao;
    private final MandateService mandateService;

    @Inject
    public GovUkPayEventService(GovUkPayEventDao govUkPayEventDao, MandateService mandateService) {
        this.govUkPayEventDao = govUkPayEventDao;
        this.mandateService = mandateService;
    }

    @Override
    public void handleEvents(List<GovUkPayEvent> events) {
        events.forEach(govUkPayEventDao::insert);

        getMandateIds(events).forEach(mandateService::updateMandateStatus);
    }

    private static Set<MandateExternalId> getMandateIds(List<GovUkPayEvent> events) {
        return events.stream()
                .map(GovUkPayEvent::getMandateId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
