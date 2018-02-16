package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerConflictException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Optional;

public class PaymentConfirmService {

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
    public void confirm(Long accountId, String paymentExternalId) {
        Transaction transaction = transactionService.confirmedDirectDebitDetailsFor(accountId, paymentExternalId);
        payerDao.findByPaymentRequestId(transaction.getPaymentRequestId())
                .map(payer -> {
                    mandateDao.insert(new Mandate(payer.getId()));
                    return Optional.of(payer.getPaymentRequestId());
                })
                .orElseThrow(() -> new PayerConflictException(String.format("Expected payment request %s to be already associated with a payer", paymentExternalId)));
        transactionService.mandateCreatedFor(transaction);
    }
}
