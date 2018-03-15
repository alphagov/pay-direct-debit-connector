
package uk.gov.pay.directdebit.webhook.gocardless.services;

import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

public interface GoCardlessAction {
    PaymentRequestEvent process(TransactionService transactionService, Transaction transaction);
}
