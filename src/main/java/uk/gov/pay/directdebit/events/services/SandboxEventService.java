package uk.gov.pay.directdebit.events.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;

import javax.inject.Inject;

public class SandboxEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxEventService.class);
    private final SandboxEventDao sandBoxEventDao;

    @Inject
    public SandboxEventService(SandboxEventDao sandBoxEventDao) {
        this.sandBoxEventDao = sandBoxEventDao;
    }

    public void insertEvent(SandboxEvent sandboxEvent) {
        sandBoxEventDao.insert(sandboxEvent);
        LOGGER.info("Inserted Sandbox event with mandate id {} ",
                sandboxEvent.getMandateId()
                        .map(SandboxMandateId::toString)
                        .orElse("No mandate Id available on the sandbox event"));
    }
}
