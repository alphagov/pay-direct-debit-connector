package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessMandateStateCalculator;
import uk.gov.pay.directdebit.mandate.services.sandbox.SandboxMandateStateCalculator;

import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

public class MandateStateUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateStateUpdater.class);

    private final MandateUpdateService mandateUpdateService;
    private final GoCardlessMandateStateCalculator goCardlessMandateStateCalculator;
    private final SandboxMandateStateCalculator sandboxMandateStateCalculator;

    @Inject
    public MandateStateUpdater(MandateUpdateService mandateUpdateService,
                               GoCardlessMandateStateCalculator goCardlessMandateStateCalculator,
                               SandboxMandateStateCalculator sandboxMandateStateCalculator) {
        this.mandateUpdateService = mandateUpdateService;
        this.goCardlessMandateStateCalculator = goCardlessMandateStateCalculator;
        this.sandboxMandateStateCalculator = sandboxMandateStateCalculator;
    }

    public void updateState(Mandate mandate) {
        getStateCalculator(mandate)
                .calculate(mandate)
                .ifPresentOrElse(stateAndDetails -> mandateUpdateService.updateState(mandate, stateAndDetails),
                        () -> LOGGER.info(format("Asked to update the status for mandate %s but there appear to be " +
                                "no events stored that require it to be updated", mandate.getExternalId())));
    }

    private MandateStateCalculator getStateCalculator(Mandate mandate) {
        return mandate.getGatewayAccount().getPaymentProvider() == GOCARDLESS
                ? goCardlessMandateStateCalculator
                : sandboxMandateStateCalculator;
    }
}
