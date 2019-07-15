package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.services.MandateUpdateService;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

public class GoCardlessMandateStateUpdater {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessMandateStateUpdater.class);

    private final MandateUpdateService mandateUpdateService;
    private final GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    @Inject
    GoCardlessMandateStateUpdater(MandateUpdateService mandateUpdateService, GoCardlessMandateStateCalculator goCardlessMandateStateCalculator) {
        this.mandateUpdateService = mandateUpdateService;
        this.goCardlessMandateStateCalculator = goCardlessMandateStateCalculator;
    }

    public void updateState(GoCardlessMandateIdAndOrganisationId goCardlessMandateIdAndOrganisationId) {
        goCardlessMandateStateCalculator.calculate(goCardlessMandateIdAndOrganisationId)
                .ifPresentOrElse(stateAndDetails -> {
                    int updated = mandateUpdateService.updateStateByPaymentProviderMandateId(GOCARDLESS, goCardlessMandateIdAndOrganisationId, stateAndDetails);
                    if (updated == 1) {
                        LOGGER.info(format("Updated status of GoCardless mandate %s for organisation %s to %s", 
                                goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(),
                                goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId(),
                                stateAndDetails.getState()));
                    } else {
                        LOGGER.error(format("Could not update status of GoCardless mandate %s for organisation %s to %s because the mandate was not found",
                                goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(),
                                goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId(), stateAndDetails.getState()));
                    }
                }, () -> LOGGER.info(format("Asked to update the status for GoCardless mandate %s for organisation %s " +
                                "but there appear to be no events stored that require it to be updated",
                        goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(), goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId())));
    }

}
