package uk.gov.pay.directdebit.payers.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.EncryptionConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Map;

public class PayerService {
    private static final Logger logger = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final PayerDao payerDao;
    private final TransactionService transactionService;

    private final PayerParser payerParser;

    public PayerService(EncryptionConfig encryptionConfig, PayerDao payerDao, TransactionService transactionService) {
        this.payerDao = payerDao;
        this.transactionService = transactionService;
        this.payerParser = new PayerParser(encryptionConfig);
    }

    public Payer create(String paymentRequestExternalId, Map<String, String> createPayerMap) {
        Transaction transaction = transactionService.receiveDirectDebitDetailsFor(paymentRequestExternalId);
        Payer payer = payerParser.parse(createPayerMap, transaction);
        Long id = payerDao.insert(payer);
        payer.setId(id);
        logger.info("Created Payer with external id {}", payer.getExternalId());
        transactionService.payerCreatedFor(transaction);
        return payer;
    }


}
