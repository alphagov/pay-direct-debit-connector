package uk.gov.pay.directdebit.events.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.SandboxEvent;

import javax.inject.Inject;

public class SandboxEventService {

    private final SandboxEventDao sandBoxEventDao;
    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxEventService.class);

    @Inject
    public SandboxEventService(SandboxEventDao sandBoxEventDao) {
        this.sandBoxEventDao = sandBoxEventDao;
    }

    public void insertEvent(SandboxEvent sandboxEvent) {
        sandBoxEventDao.insert(sandboxEvent);
        LOGGER.info("Inserted Sandbox event with mandate id {} ", sandboxEvent.getMandateId());
    }
}
