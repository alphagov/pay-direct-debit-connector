package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;

import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

public class GoCardlessMandateStateUpdater {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessMandateStateUpdater.class);

    private final MandateDao mandateDao;
    private final GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    @Inject
    GoCardlessMandateStateUpdater(MandateDao mandateDao, GoCardlessMandateStateCalculator goCardlessMandateStateCalculator) {
        this.mandateDao = mandateDao;
        this.goCardlessMandateStateCalculator = goCardlessMandateStateCalculator;
    }

    public void updateState(GoCardlessMandateId goCardlessMandateId, GoCardlessOrganisationId goCardlessOrganisationId) {
        goCardlessMandateStateCalculator.calculate(goCardlessMandateId, goCardlessOrganisationId)
                .ifPresentOrElse(state -> {
                    int updated = mandateDao.updateStateByPaymentProviderMandateId(GOCARDLESS, goCardlessOrganisationId, goCardlessMandateId, state);
                    if (updated == 1) {
                        LOGGER.info(format("Updated status of GoCardless mandate %s for organisation %s to %s", goCardlessMandateId, goCardlessOrganisationId,
                                state));
                    } else {
                        LOGGER.error(format("Could not update status of GoCardless mandate %s for organisation %s to %s because the mandate was not found",
                                goCardlessMandateId, goCardlessOrganisationId, state));
                    }
                }, () -> LOGGER.info(format("Asked to update the status for GoCardless mandate %s for organisation %s " +
                                "but there appear to be no events stored that require it to be updated", goCardlessMandateId, goCardlessOrganisationId)));
    }

}
