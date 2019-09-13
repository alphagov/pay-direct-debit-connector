package uk.gov.pay.directdebit.payments.services.sandbox;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.events.dao.GovUkPayEventDao;
import uk.gov.pay.directdebit.events.dao.SandboxEventDao;
import uk.gov.pay.directdebit.events.model.Event;
import uk.gov.pay.directdebit.events.model.GovUkPayEvent;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentStateCalculator;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.payments.services.GovUkPayEventToPaymentStateMapper.GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.payments.services.GovUkPayEventToPaymentStateMapper.mapGovUkPayEventToPaymentState;
import static uk.gov.pay.directdebit.payments.services.sandbox.SandboxEventToPaymentStateMapper.SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE;
import static uk.gov.pay.directdebit.payments.services.sandbox.SandboxEventToPaymentStateMapper.mapSandboxEventToPaymentState;

public class SandboxPaymentStateCalculator implements PaymentStateCalculator {

    private final SandboxEventDao sandboxEventDao;
    private final GovUkPayEventDao govUkPayEventDao;

    @Inject
    SandboxPaymentStateCalculator(SandboxEventDao sandboxEventDao,
                                  GovUkPayEventDao govUkPayEventDao) {
        this.sandboxEventDao = sandboxEventDao;
        this.govUkPayEventDao = govUkPayEventDao;
    }

    public Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment) {
        Optional<SandboxEvent> latestApplicableSandboxEvent = getLatestApplicableSandboxEvent(payment);

        Optional<GovUkPayEvent> latestApplicableGovUkPayEvent
                = govUkPayEventDao.findLatestApplicableEventForPayment(payment.getId(), GOV_UK_PAY_EVENT_TYPES_THAT_CHANGE_PAYMENT_STATE);

        return Stream.of(latestApplicableSandboxEvent, latestApplicableGovUkPayEvent)
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Event::getTimestamp))
                .flatMap(this::mapEventToState);
    }

    private Optional<SandboxEvent> getLatestApplicableSandboxEvent(Payment payment) {
        return payment.getProviderId().flatMap(providerId ->
                sandboxEventDao.findLatestApplicableEventForPayment(
                        (SandboxPaymentId) providerId,
                        SANDBOX_ACTIONS_THAT_CHANGE_PAYMENT_STATE));
    }

    private Optional<DirectDebitStateWithDetails<PaymentState>> mapEventToState(Event event) {
        if (event instanceof SandboxEvent) {
            return mapSandboxEventToPaymentState((SandboxEvent) event);
        } else if (event instanceof GovUkPayEvent) {
            return mapGovUkPayEventToPaymentState((GovUkPayEvent) event);
        } else {
            throw new IllegalArgumentException(format("Unexpected Event of type %s", event.getClass()));
        }
    }
}
