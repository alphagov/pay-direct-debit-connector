package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateUpdateService;

import javax.inject.Inject;

import static java.lang.String.format;

public class GoCardlessMandateStateUpdater {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessMandateStateUpdater.class);

    private final MandateUpdateService mandateUpdateService;
    private final GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;

    @Inject
    GoCardlessMandateStateUpdater(MandateUpdateService mandateUpdateService, GoCardlessMandateStateCalculator goCardlessMandateStateCalculator) {
        this.mandateUpdateService = mandateUpdateService;
        this.goCardlessMandateStateCalculator = goCardlessMandateStateCalculator;
    }

    public void updateState(Mandate mandate) {
        goCardlessMandateStateCalculator.calculate(mandate)
                .ifPresentOrElse(stateAndDetails -> {
                    mandateUpdateService.updateState(mandate, stateAndDetails);
                }, () -> LOGGER.info(format("Asked to update the status for mandate %s but there appear to be no events " +
                        "stored that require it to be updated", mandate.getExternalId())));
    }
}
