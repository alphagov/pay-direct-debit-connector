package uk.gov.pay.directdebit.mandate.services;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentConfirmService.class);
    private final TransactionService transactionService;
    private final MandateDao mandateDao;
    private final PayerDao payerDao;

    @Inject
    public PaymentConfirmService(TransactionService transactionService, PayerDao payerDao, MandateDao mandateDao) {
        this.transactionService = transactionService;
        this.payerDao = payerDao;
        this.mandateDao = mandateDao;
    }

    /**
     * Creates a mandate and updates the transaction to a pending (Sandbox)
     *
     * @param paymentExternalId
     */
    public ConfirmationDetails confirm(String accountExternalId, String paymentExternalId, Map<String, String> confirmDetailsRequest) {
        Transaction transaction = transactionService.confirmedDirectDebitDetailsFor(accountExternalId, paymentExternalId);
        String accountNumber = confirmDetailsRequest.get("account_number");
        String sortCode = confirmDetailsRequest.get("sort_code");
        Long paymentRequestId = transaction.getPaymentRequest().getId();
        Mandate createdMandate = payerDao.findByPaymentRequestId(paymentRequestId)
                .map(this::createMandateFor)
                .orElseThrow(() -> new PayerConflictException(String.format("Expected payment request %s to be already associated with a payer", paymentExternalId)));
        LOGGER.info("Mandate created for payment request {}", paymentRequestId);
        return new ConfirmationDetails(transaction, createdMandate, accountNumber, sortCode);
    }

    private Mandate createMandateFor(Payer payer) {
        //todo use a real reference when playing PP-3547
        Mandate mandate = new Mandate(payer.getId(), "TEMP_REFERENCE");
        Long id = mandateDao.insert(mandate);
        mandate.setId(id);
        return mandate;
    }
}
