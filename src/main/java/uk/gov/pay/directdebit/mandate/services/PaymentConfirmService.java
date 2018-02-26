package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerConflictException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Optional;

public class PaymentConfirmService {

    public static class ConfirmationDetails {
        private Transaction transaction;
        private Mandate mandate;

        public ConfirmationDetails(Transaction transaction, Mandate mandate) {
            this.transaction = transaction;
            this.mandate = mandate;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction transaction) {
            this.transaction = transaction;
        }

        public Mandate getMandate() {
            return mandate;
        }

        public void setMandate(Mandate mandate) {
            this.mandate = mandate;
        }
    }
    private final MandateDao mandateDao;
    private final PayerDao payerDao;
    private final TransactionService transactionService;

    public PaymentConfirmService(TransactionService transactionService, PayerDao payerDao, MandateDao mandateDao) {
        this.transactionService = transactionService;
        this.payerDao = payerDao;
        this.mandateDao = mandateDao;
    }


    /**
     * Creates a mandate and updates the transaction to a pending (Sandbox)
     * @param paymentExternalId
     */
    public ConfirmationDetails confirm(Long accountId, String paymentExternalId) {
        Transaction transaction = transactionService.confirmedDirectDebitDetailsFor(accountId, paymentExternalId);
        Mandate createdMandate = payerDao.findByPaymentRequestId(transaction.getPaymentRequestId())
                .map(this::createMandateFor)
                .orElseThrow(() -> new PayerConflictException(String.format("Expected payment request %s to be already associated with a payer", paymentExternalId)));
        transactionService.mandateCreatedFor(transaction);
        return new ConfirmationDetails(transaction, createdMandate);
    }

    private Mandate createMandateFor(Payer payer) {
        Mandate mandate = new Mandate(payer.getId());
        Long id = mandateDao.insert(mandate);
        mandate.setId(id);
        return mandate;
    }
}
