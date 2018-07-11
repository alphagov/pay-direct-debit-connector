package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;

public class MandateStateUpdateService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final DirectDebitEventService directDebitEventService;

    @Inject
    public MandateStateUpdateService(
            MandateDao mandateDao,
            DirectDebitEventService directDebitEventService) {
        this.directDebitEventService = directDebitEventService;
        this.mandateDao = mandateDao;
    }
    

    Mandate confirmedDirectDebitDetailsFor(Mandate mandate) {
        updateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);
        directDebitEventService.registerDirectDebitConfirmedEventFor(mandate);
        return mandate;
    }

    private Mandate updateStateFor(Mandate mandate, DirectDebitEvent.SupportedEvent event) {
        MandateState newState = MandateStatesGraph.getStates().getNextStateForEvent(mandate.getState(),
                event);
        mandateDao.updateState(mandate.getId(), newState);
        LOGGER.info("Updating mandate {} - from {} to {}",
                mandate.getExternalId(),
                mandate.getState(),
                newState);
        mandate.setState(newState);
        return mandate;
    }
    
    void canUpdateStateFor(Mandate mandate, DirectDebitEvent.SupportedEvent event) {
        MandateStatesGraph.getStates().getNextStateForEvent(mandate.getState(),
                event);
    }
}
