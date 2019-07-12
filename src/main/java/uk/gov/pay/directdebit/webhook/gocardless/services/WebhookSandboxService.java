package uk.gov.pay.directdebit.webhook.gocardless.services;

import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.services.sandbox.SandboxPaymentStateUpdater;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class WebhookSandboxService {

    private final SandboxPaymentStateUpdater sandboxPaymentStateUpdater;

    @Inject
    WebhookSandboxService(SandboxPaymentStateUpdater sandboxPaymentStateUpdater) {
        this.sandboxPaymentStateUpdater = sandboxPaymentStateUpdater;
    }

    public void updateStateOfPaymentsAffectedByEvents(List<SandboxEvent> events) {
        events.stream()
                .map(SandboxEvent::getPaymentId)
                .flatMap(Optional::stream)
                .distinct()
                .forEach(sandboxPaymentStateUpdater::updateState);
    }

}
