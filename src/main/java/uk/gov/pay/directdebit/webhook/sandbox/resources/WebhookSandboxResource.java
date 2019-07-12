package uk.gov.pay.directdebit.webhook.sandbox.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.events.services.SandboxEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;
import uk.gov.pay.directdebit.payments.services.PaymentService;
import uk.gov.pay.directdebit.webhook.gocardless.services.WebhookSandboxService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/sandbox")
public class WebhookSandboxResource {

    private final PaymentService paymentService;
    private final SandboxEventService sandboxEventService;
    private final WebhookSandboxService webhookSandboxService;

    @Inject
    public WebhookSandboxResource(PaymentService paymentService, SandboxEventService sandboxEventService, WebhookSandboxService webhookSandboxService) {
        this.paymentService = paymentService;
        this.sandboxEventService = sandboxEventService;
        this.webhookSandboxService = webhookSandboxService;
    }

    public enum SandboxEventAction {
        PAID_OUT
    }

    enum SandboxEventCause {
        PAID_OUT_CAUSE
    }

    @POST
    @Timed
    public Response handleWebhook() {
        paymentService.findAllByPaymentStateAndProvider(PaymentState.PENDING, PaymentProvider.SANDBOX)
                .forEach(this::processPayment);
        return Response.status(OK).build();
    }

    private void processPayment(Payment payment) {
        var event = createSandboxEventFromPayment(payment);
        sandboxEventService.insertEvent(event);
        paymentService.paymentPaidOutFor(payment);
        webhookSandboxService.updateStateOfPaymentsAffectedByEvents(List.of(event));
    }

    private SandboxEvent createSandboxEventFromPayment(Payment payment){
        return SandboxEvent.SandboxEventBuilder.aSandboxEvent()
                .withPaymentId(SandboxPaymentId.valueOf(payment.getExternalId()))
                .withEventAction(SandboxEventAction.PAID_OUT.toString())
                .withEventCause(SandboxEventCause.PAID_OUT_CAUSE.toString())
                .withCreatedAt(ZonedDateTime.now())
                .withMandateId(SandboxMandateId.valueOf(payment.getMandate().getId().toString()))
                .build();
    }
}
