package uk.gov.pay.directdebit.webhook.sandbox.resources;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/v1/webhooks/sandbox")
public class WebhookSandboxResource {

    private final TransactionService transactionService;

    @Inject
    public WebhookSandboxResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @POST
    @Timed
    public Response handleWebhook() {
        List<Transaction> pendingTransactions = transactionService.findAllByPaymentStateAndProvider(PaymentState.PENDING, PaymentProvider.SANDBOX);
        pendingTransactions.forEach(transactionService::paymentPaidOutFor);

        return Response.status(OK).build();
    }

}
