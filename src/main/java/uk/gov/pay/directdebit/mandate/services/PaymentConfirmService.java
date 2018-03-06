package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerConflictException;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;

public class PaymentConfirmService {
    private final MandateDao mandateDao;
    private final PayerDao payerDao;
    private final TransactionService transactionService;

    @Inject
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
