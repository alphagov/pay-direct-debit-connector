package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentStateUpdater;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public class WebhookSandboxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookSandboxService.class);

    private final PaymentStateUpdater paymentStateUpdater;
    private final PaymentQueryService paymentQueryService;

    @Inject
    WebhookSandboxService(PaymentStateUpdater paymentStateUpdater, PaymentQueryService paymentQueryService) {
        this.paymentStateUpdater = paymentStateUpdater;
        this.paymentQueryService = paymentQueryService;
    }

    public void updateStateOfPaymentsAffectedByEvents(List<SandboxEvent> events) {
        events.stream()
                .map(SandboxEvent::getPaymentId)
                .flatMap(Optional::stream)
                .distinct()
                .map(sandboxPaymentId -> paymentQueryService.findBySandboxPaymentId(sandboxPaymentId)
                        .or(() -> {
                            LOGGER.error(format("Could not update status of Sandbox payment %s because the payment was not found",
                                    sandboxPaymentId));
                            return Optional.empty();
                        }))
                .flatMap(Optional::stream)
                .forEach(paymentStateUpdater::updateStateIfNecessary);
    }
}
