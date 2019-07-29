package uk.gov.pay.directdebit.tasks.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;

public class ExpireService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpireService.class);

    private final MandateQueryService mandateQueryService;
    private final GovUkPayEventService govUkPayEventService;

    private static final long MIN_EXPIRY_AGE_MINUTES = 90L;
    private static final Set<MandateState> EXPIRABLE_STATES = Set.of(MandateState.CREATED, AWAITING_DIRECT_DEBIT_DETAILS);

    @Inject
    ExpireService(MandateQueryService mandateQueryService,
                  GovUkPayEventService govUkPayEventService) {
        this.mandateQueryService = mandateQueryService;
        this.govUkPayEventService = govUkPayEventService;
    }

    public int expireMandates() {
        LOGGER.info("Starting expire mandates process.");
        List<Mandate> mandatesToExpire = getMandatesForExpiration();
        for (Mandate mandate : mandatesToExpire) {
            govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, GovUkPayEventType.MANDATE_USER_SETUP_EXPIRED);
            LOGGER.info("Expired mandate " + mandate.getId());
        }
        return mandatesToExpire.size();
    }

    private List<Mandate> getMandatesForExpiration() {
        ZonedDateTime cutOffTime = ZonedDateTime.now().minusMinutes(MIN_EXPIRY_AGE_MINUTES);
        return mandateQueryService.findAllMandatesBySetOfStatesAndMaxCreationTime(EXPIRABLE_STATES, cutOffTime);
    }
}
