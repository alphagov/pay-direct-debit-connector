package uk.gov.pay.directdebit.payments.services.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentUpdateService;

import javax.inject.Inject;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

public class SandboxPaymentStateUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxPaymentStateUpdater.class);

    private final PaymentUpdateService paymentUpdateService;
    private final SandboxPaymentStateCalculator sandboxPaymentStateCalculator;

    @Inject
    SandboxPaymentStateUpdater(PaymentUpdateService paymentUpdateService, SandboxPaymentStateCalculator sandboxPaymentStateCalculator) {
        this.paymentUpdateService = paymentUpdateService;
        this.sandboxPaymentStateCalculator = sandboxPaymentStateCalculator;
    }

    public void updateState(SandboxPaymentId sandboxPaymentId) {
        sandboxPaymentStateCalculator.calculate(sandboxPaymentId)
                .ifPresentOrElse(state -> {
                    int updated = paymentUpdateService.updateStateByProviderId(SANDBOX, sandboxPaymentId, state);
                    if (updated == 1) {
                        LOGGER.info(format("Updated status of sandbox payment %s to %s", sandboxPaymentId, state));
                    } else {
                        LOGGER.error(format("Could not update status of sandbox payment %s to %s because the payment was not found", sandboxPaymentId, state));
                    }
                }, () -> LOGGER.info(format("Asked to update the status for sandbox payment %s " +
                                "but there appear to be no events stored that require it to be updated", sandboxPaymentId)));
    }

}
