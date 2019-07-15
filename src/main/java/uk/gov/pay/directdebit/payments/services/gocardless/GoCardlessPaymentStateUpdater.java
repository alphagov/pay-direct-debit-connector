package uk.gov.pay.directdebit.payments.services.gocardless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.services.PaymentUpdateService;

import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

public class GoCardlessPaymentStateUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessPaymentStateUpdater.class);

    private final PaymentUpdateService paymentUpdateService;
    private final GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator;

    @Inject
    GoCardlessPaymentStateUpdater(PaymentUpdateService paymentUpdateService, GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator) {
        this.paymentUpdateService = paymentUpdateService;
        this.goCardlessPaymentStateCalculator = goCardlessPaymentStateCalculator;
    }

    public void updateState(GoCardlessPaymentIdAndOrganisationId goCardlessPaymentIdAndOrganisationId) {
        goCardlessPaymentStateCalculator.calculate(goCardlessPaymentIdAndOrganisationId)
                .ifPresentOrElse(stateAndDetails -> {
                    int updated = paymentUpdateService.updateStateByProviderId(GOCARDLESS, goCardlessPaymentIdAndOrganisationId, stateAndDetails);
                    if (updated == 1) {
                        LOGGER.info(format("Updated status of GoCardless payment %s for organisation %s to %s",
                                goCardlessPaymentIdAndOrganisationId.getGoCardlessPaymentId(),
                                goCardlessPaymentIdAndOrganisationId.getGoCardlessOrganisationId(),
                                stateAndDetails.getState()));
                    } else {
                        LOGGER.error(format("Could not update status of GoCardless payment %s for organisation %s to %s because the payment was not found",
                                goCardlessPaymentIdAndOrganisationId.getGoCardlessPaymentId(),
                                goCardlessPaymentIdAndOrganisationId.getGoCardlessOrganisationId(), stateAndDetails.getState()));
                    }
                }, () -> LOGGER.info(format("Asked to update the status for GoCardless payment %s for organisation %s " +
                                "but there appear to be no events stored that require it to be updated",
                        goCardlessPaymentIdAndOrganisationId.getGoCardlessPaymentId(), goCardlessPaymentIdAndOrganisationId.getGoCardlessOrganisationId())));
    }
    
}
