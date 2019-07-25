package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEventType;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.PaymentStateCalculator;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PAID_OUT;

public class GoCardlessPaymentStateCalculator implements PaymentStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;
    private final GovUkPayEventDao govUkPayEventDao;

    private static final Map<String, PaymentState> GOCARDLESS_ACTION_TO_PAYMENT_STATE = Map.of(
            "failed", FAILED,
            "paid_out", PAID_OUT
    );

    static final Set<String> GOCARDLESS_ACTIONS_THAT_CHANGE_STATE = GOCARDLESS_ACTION_TO_PAYMENT_STATE.keySet();
    
    private static final Map<GovUkPayEventType, PaymentState> GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE = Map.of(
            PAYMENT_SUBMITTED, PaymentState.SUBMITTED_TO_PROVIDER
    );
    
    static final Set<GovUkPayEventType> GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE = GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE.keySet();

    @Inject
    GoCardlessPaymentStateCalculator(GoCardlessEventDao goCardlessEventDao,
                                     GovUkPayEventDao govUkPayEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment) {
        Optional<GoCardlessEvent> latestApplicableGoCardlessEvent = getLatestApplicableGoCardlessEvent(payment);

        Optional<GovUkPayEvent> latestApplicableGovUkPayEvent 
                = govUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_STATE);
        
        return Stream.of(latestApplicableGoCardlessEvent,latestApplicableGovUkPayEvent)
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Event::getTimestamp))
                .map(this::mapEventToState);
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

    private DirectDebitStateWithDetails<PaymentState> mapEventToState(Event event) {
        if (event instanceof GoCardlessEvent) {
            return mapGoCardlessEventToState((GoCardlessEvent) event);
        } else if (event instanceof GovUkPayEvent) {
            return mapGovUkPayEventToState((GovUkPayEvent) event);
        } else {
            throw new IllegalArgumentException(format("Unexpected Event of type %s", event.getClass()));
        }
    }

    private DirectDebitStateWithDetails<PaymentState> mapGoCardlessEventToState(GoCardlessEvent goCardlessEvent) {
        return new DirectDebitStateWithDetails<>(
                GOCARDLESS_ACTION_TO_PAYMENT_STATE.get(goCardlessEvent.getAction()),
                goCardlessEvent.getDetailsCause(),
                goCardlessEvent.getDetailsDescription());
    }
    
    private DirectDebitStateWithDetails<PaymentState> mapGovUkPayEventToState(GovUkPayEvent govUkPayEvent) {
        return new DirectDebitStateWithDetails<>(
                GOV_UK_PAY_EVENT_TYPE_TO_PAYMENT_STATE.get(govUkPayEvent.getEventType()));
    }
}
