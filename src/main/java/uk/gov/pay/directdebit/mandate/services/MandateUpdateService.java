package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import javax.inject.Inject;

import static java.lang.String.format;

public class MandateUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateUpdateService.class);
    
    private final MandateDao mandateDao;

    @Inject
    MandateUpdateService(MandateDao mandateDao) {
        this.mandateDao = mandateDao;
    }

    public Mandate updateState(Mandate mandate, DirectDebitStateWithDetails<MandateState> stateAndDetails) {
        
        String details = stateAndDetails.getDetails().orElse(null);
        String description = stateAndDetails.getDetailsDescription().orElse(null);
        
        mandateDao.updateStateAndDetails(mandate.getId(),
                stateAndDetails.getState(),
                details,
                description);

        LOGGER.info(format("Updated status of mandate %s to %s", mandate.getExternalId(), stateAndDetails.getState()));

        return Mandate.MandateBuilder.fromMandate(mandate)
                .withState(stateAndDetails.getState())
                .withStateDetails(details)
                .withStateDetailsDescription(description)
                .build();
    }
    
}
