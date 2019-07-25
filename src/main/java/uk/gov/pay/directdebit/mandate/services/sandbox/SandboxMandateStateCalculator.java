package uk.gov.pay.directdebit.mandate.services.sandbox;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper;
import uk.gov.pay.directdebit.mandate.services.MandateStateCalculator;

import javax.inject.Inject;
import java.util.Optional;

import static uk.gov.pay.directdebit.mandate.services.GovUkPayEventToMandateStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE;

public class SandboxMandateStateCalculator implements MandateStateCalculator {
    private final GovUkPayEventDao govUkPayEventDao;

    @Inject
    public SandboxMandateStateCalculator(GovUkPayEventDao govUkPayEventDao) {
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<DirectDebitStateWithDetails<MandateState>> calculate(Mandate mandate) {
        return govUkPayEventDao.findLatestApplicableEventForMandate(mandate.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_MANDATE_STATE)
                .flatMap(GovUkPayEventToMandateStateMapper::mapGovUkPayEventToMandateState);
    }
}
