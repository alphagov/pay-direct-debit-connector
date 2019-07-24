package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.PaymentStateCalculator;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

public class GoCardlessPaymentStateCalculator implements PaymentStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;

    private static final Map<String, PaymentState> GOCARDLESS_ACTION_TO_PAYMENT_STATE = Map.of(
            "failed", FAILED,
            "paid_out", SUCCESS
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_STATE = GOCARDLESS_ACTION_TO_PAYMENT_STATE.keySet();

    @Inject
    GoCardlessPaymentStateCalculator(GoCardlessEventDao goCardlessEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
    }

    public Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment) {
        return getLatestApplicableGoCardlessEvent(payment)
                .filter(goCardlessEvent -> GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()) != null)
                .map(goCardlessEvent -> new DirectDebitStateWithDetails<>(
                        GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()),
                        goCardlessEvent.getDetailsCause(),
                        goCardlessEvent.getDetailsDescription())
                );
    }

    private Optional<GoCardlessEvent> getLatestApplicableGoCardlessEvent(Payment payment) {
        return payment.getProviderId()
                .flatMap(providerId -> {
                    GoCardlessOrganisationId goCardlessOrganisationId = payment.getMandate().getGatewayAccount().getOrganisation()
                            .orElseThrow(() -> new GatewayAccountMissingOrganisationIdException(payment.getMandate().getGatewayAccount()));

                    return goCardlessEventDao.findLatestApplicableEventForPayment(
                            (GoCardlessPaymentId) providerId,
                            goCardlessOrganisationId,
                            GOCARDLESS_ACTIONS_THAT_CHANGE_STATE);
                });
    }
}
