package uk.gov.pay.directdebit.payments.services.gocardless;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountMissingOrganisationIdException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.services.PaymentStateCalculator;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.payments.services.GovUkPayEventToPaymentStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.payments.services.GovUkPayEventToPaymentStateMapper.mapGovUkPayEventToPaymentState;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessEventToPaymentStateMapper.GOCARDLESS_ACTIONS_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.payments.services.gocardless.GoCardlessEventToPaymentStateMapper.mapGoCardlessEventToPaymentState;

public class GoCardlessPaymentStateCalculator implements PaymentStateCalculator {

    private final GoCardlessEventDao goCardlessEventDao;
    private final GovUkPayEventDao govUkPayEventDao;

    @Inject
    GoCardlessPaymentStateCalculator(GoCardlessEventDao goCardlessEventDao,
                                     GovUkPayEventDao govUkPayEventDao) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment) {
        Optional<GoCardlessEvent> latestApplicableGoCardlessEvent = getLatestApplicableGoCardlessEvent(payment);

        Optional<GovUkPayEvent> latestApplicableGovUkPayEvent
                = govUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE);

        return Stream.of(latestApplicableGoCardlessEvent, latestApplicableGovUkPayEvent)
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Event::getTimestamp))
                .flatMap(this::mapEventToState);
    }

    private Optional<GoCardlessEvent> getLatestApplicableGoCardlessEvent(Payment payment) {
        return payment.getProviderId()
                .flatMap(providerId -> {
                    GoCardlessOrganisationId goCardlessOrganisationId = payment.getMandate().getGatewayAccount().getOrganisation()
                            .orElseThrow(() -> new GatewayAccountMissingOrganisationIdException(payment.getMandate().getGatewayAccount()));

                    return goCardlessEventDao.findLatestApplicableEventForPayment(
                            (GoCardlessPaymentId) providerId,
                            goCardlessOrganisationId,
                            GOCARDLESS_ACTIONS_THAT_CHANGE_PAYMENT_STATE);
                });
    }

    private Optional<DirectDebitStateWithDetails<PaymentState>> mapEventToState(Event event) {
        if (event instanceof GoCardlessEvent) {
            return mapGoCardlessEventToPaymentState((GoCardlessEvent) event);
        } else if (event instanceof GovUkPayEvent) {
            return mapGovUkPayEventToPaymentState((GovUkPayEvent) event);
        } else {
            throw new IllegalArgumentException(format("Unexpected Event of type %s", event.getClass()));
        }
    }
}
