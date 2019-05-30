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
import java.util.List;

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
        SANBOX_EVENT_ACTION
    }
    
    enum SandboxEventCause {
        SANDBOX_EVENT_CAUSE
    }

    @POST
    @Timed
    public Response handleWebhook() {
        List<Transaction> pendingTransactions = transactionService.findAllByPaymentStateAndProvider(PaymentState.PENDING, PaymentProvider.SANDBOX);
        pendingTransactions
                .stream()
                .map(x -> parseTransaction(x, 
                        SandboxEventAction.SANBOX_EVENT_ACTION,
                        SandboxEventCause.SANDBOX_EVENT_CAUSE ))
                .forEach(sandboxEventService::insertEvent);
        pendingTransactions.forEach(transactionService::paymentPaidOutFor);
        return Response.status(OK).build();
    }

    private SandboxEvent parseTransaction(Transaction transaction, SandboxEventAction sandboxEventAction,
                                          SandboxEventCause sandboxEventCause){
        return new SandboxEvent(
                transaction.getMandate().getId(),
                transaction.getId(),
                sandboxEventAction.toString(),
                sandboxEventCause.toString(),
                ZonedDateTime.now());
    }
}
