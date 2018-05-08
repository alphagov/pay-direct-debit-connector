package uk.gov.pay.directdebit.payers.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import java.util.Map;

public class PayerService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PayerService.class);

    private final PayerDao payerDao;
    private final TransactionService transactionService;
    private final PayerParser payerParser;

    @Inject
    public PayerService(PayerDao payerDao,
                        TransactionService transactionService,
                        PayerParser payerParser) {
        this.payerDao = payerDao;
        this.transactionService = transactionService;
        this.payerParser = payerParser;
    }

    public Payer getPayerFor(Transaction transaction) {
        PaymentRequest paymentRequest = transaction.getPaymentRequest();
        return payerDao
                .findByPaymentRequestId(paymentRequest.getId())
                .orElseThrow(() -> new PayerNotFoundException(paymentRequest.getExternalId()));
    }

    public Payer createOrUpdatePayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        Transaction transaction = transactionService.receiveDirectDebitDetailsFor(gatewayAccount.getExternalId(), paymentRequestExternalId);
        Payer payerDetails = payerParser.parse(createPayerRequest, transaction);

        return payerDao
                .findByPaymentRequestId(transaction.getPaymentRequest().getId())
                .map(oldPayer -> editPayer(transaction, oldPayer, payerDetails))
                .orElseGet(() -> create(transaction, payerDetails));
    }

    private Payer editPayer(Transaction transaction, Payer oldPayer, Payer newPayer) {
        LOGGER.info("Updating payer with external id {}", oldPayer.getExternalId());
        payerDao.updatePayerDetails(oldPayer.getId(), newPayer);
        transactionService.payerEditedFor(transaction);
        return newPayer;
    }

    public Payer create(Transaction transaction, Payer payer) {
        Long id = payerDao.insert(payer);
        payer.setId(id);
        LOGGER.info("Created Payer with external id {}", payer.getExternalId());
        transactionService.payerCreatedFor(transaction);
        return payer;
    }
}
