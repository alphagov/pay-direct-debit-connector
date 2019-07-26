package uk.gov.pay.directdebit.tasks.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdateService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class ExpireService {
    
    private MandateStatesGraph mandateStatesGraph;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpireService.class);
    private final long MIN_EXPIRY_AGE_MINUTES = 90L;
    private final MandateState MANDATE_EXPIRY_CUTOFF_STATUS = MandateState.SUBMITTED_TO_PROVIDER;
    private final MandateQueryService mandateQueryService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    ExpireService(MandateStatesGraph mandateStatesGraph,
                  MandateQueryService mandateQueryService,
                  MandateStateUpdateService mandateStateUpdateService) {
        this.mandateQueryService = mandateQueryService;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.mandateStatesGraph = mandateStatesGraph;
    }

    public int expireMandates() {
        LOGGER.info("Starting expire mandates process.");
        List<Mandate> mandatesToExpire = getMandatesForExpiration();
        for (Mandate mandate : mandatesToExpire) {
            mandateStateUpdateService.mandateExpiredFor(mandate);
            LOGGER.info("Expired mandate " + mandate.getId());
        }
        return mandatesToExpire.size();
    }

    private List<Mandate> getMandatesForExpiration() {
        Set<MandateState> states = mandateStatesGraph.getPriorStates(MANDATE_EXPIRY_CUTOFF_STATUS);
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return mandateQueryService.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
}
