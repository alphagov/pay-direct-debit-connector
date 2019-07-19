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

    public void updateState(Mandate mandate,
                           DirectDebitStateWithDetails<MandateState> stateAndDetails) {
        mandateDao.updateStateAndDetails(mandate.getId(),
                stateAndDetails.getState(),
                stateAndDetails.getDetails().orElse(null),
                stateAndDetails.getDetailsDescription().orElse(null));

        LOGGER.info(format("Updated status of mandate %s to %s", mandate.getExternalId(), stateAndDetails.getState()));
    }

}
