package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;

public class GoCardlessPaymentStateCalculator {

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

    Optional<DirectDebitStateWithDetails<PaymentState>> calculate(GoCardlessPaymentIdAndOrganisationId goCardlessPaymentIdAndOrganisationId) {
        return goCardlessEventDao.findLatestApplicableEventForPayment(goCardlessPaymentIdAndOrganisationId, GOCARDLESS_ACTIONS_THAT_CHANGE_STATE)
                .filter(goCardlessEvent -> GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()) != null)
                .map(goCardlessEvent -> new DirectDebitStateWithDetails<>(
                        GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()),
                        goCardlessEvent.getDetailsCause(),
                        goCardlessEvent.getDetailsDescription())
                );
    }

}
