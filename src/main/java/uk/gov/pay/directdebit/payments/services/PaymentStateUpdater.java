package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidPaymentProviderException;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessPaymentStateCalculator;
import uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateCalculator;

import javax.inject.Inject;

import static java.lang.String.format;

public class PaymentStateUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentStateUpdater.class);

    private final PaymentUpdateService paymentUpdateService;
    private final GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator;
    private final SandboxPaymentStateCalculator sandboxPaymentStateCalculator;

    @Inject
    PaymentStateUpdater(PaymentUpdateService paymentUpdateService,
                        GoCardlessPaymentStateCalculator goCardlessPaymentStateCalculator,
                        SandboxPaymentStateCalculator sandboxPaymentStateCalculator) {
        this.paymentUpdateService = paymentUpdateService;
        this.goCardlessPaymentStateCalculator = goCardlessPaymentStateCalculator;
        this.sandboxPaymentStateCalculator = sandboxPaymentStateCalculator;
    }

    public Payment updateStateIfNecessary(Payment payment) {
        return getStateCalculator(payment)
                .calculate(payment)
                .map(stateAndDetails -> paymentUpdateService.updateState(payment, stateAndDetails))
                .orElseGet(() -> {
                    LOGGER.info(format("Asked to update the status for payment %s but there appear to be " +
                            "no events stored that require it to be updated", payment.getExternalId()));
                    return payment;
                });
    }
    
    private PaymentStateCalculator getStateCalculator(Payment payment) {
        switch (payment.getMandate().getGatewayAccount().getPaymentProvider()) {
            case SANDBOX:
                return sandboxPaymentStateCalculator;
            case GOCARDLESS:
                return goCardlessPaymentStateCalculator;
            default:
                throw new InvalidPaymentProviderException(payment.getMandate().getGatewayAccount().getPaymentProvider().toString());
        }
    }

}
