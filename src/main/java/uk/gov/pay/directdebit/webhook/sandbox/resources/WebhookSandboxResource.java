package uk.gov.pay.directdebit.webhook.sandbox.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.SandboxEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/sandbox")
public class WebhookSandboxResource {

    private final TransactionService transactionService;
    private final SandboxEventService sandboxEventService;

    @Inject
    public WebhookSandboxResource(TransactionService transactionService, SandboxEventService sandboxEventService) {
        this.transactionService = transactionService;
        this.sandboxEventService = sandboxEventService;
    }
    
    enum SandboxEventAction {
        SANDBOX_EVENT_ACTION
    }
    
    enum SandboxEventCause {
        SANDBOX_EVENT_CAUSE
    }

    @POST
    @Timed
    public Response handleWebhook() {
        transactionService.findAllByPaymentStateAndProvider(PaymentState.PENDING, PaymentProvider.SANDBOX)
                .forEach(this::processTransaction);
        return Response.status(OK).build();
    }
    
    private void processTransaction(Transaction transaction) {
        sandboxEventService.insertEvent(createSandboxEventFromTransaction(transaction));
        transactionService.paymentPaidOutFor(transaction);
    }

    private SandboxEvent createSandboxEventFromTransaction(Transaction transaction){
        return new SandboxEvent(
                transaction.getMandate().getId().toString(),
                transaction.getId().toString(),
                SandboxEventAction.SANDBOX_EVENT_ACTION.toString(),
                SandboxEventCause.SANDBOX_EVENT_CAUSE.toString(),
                ZonedDateTime.now());
    }
}
