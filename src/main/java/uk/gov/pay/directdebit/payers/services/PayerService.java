package uk.gov.pay.directdebit.payers.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentProviderMapper;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Map;

public class PayerService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(PaymentRequestService.class);

    private final PayerDao payerDao;
    private final TransactionService transactionService;
    private final PayerParser payerParser;
    PaymentProviderMapper paymentProviderMapper;

    public PayerService(PayerDao payerDao, TransactionService transactionService, PayerParser payerParser, PaymentProviderMapper paymentProviderMapper) {
        this.payerDao = payerDao;
        this.transactionService = transactionService;
        this.payerParser = payerParser;
        this.paymentProviderMapper = paymentProviderMapper;
    }

    private PaymentProvider retrievePaymentProviderFor(Long accountId, String paymentRequestExternalId) {
        return transactionService
                .findChargeForExternalIdAndGatewayAccountId(paymentRequestExternalId, accountId)
                .getPaymentProvider();
    }

    public void createCustomerFor(Long accountId, String paymentRequestExternalId, Payer payer, String sortCode, String accountNumber) {
        PaymentProvider provider = retrievePaymentProviderFor(accountId, paymentRequestExternalId);
        LOGGER.info("Creating payer and bank account details for provider {}, payment request with id: {}", provider.toString(), paymentRequestExternalId);

        paymentProviderMapper
                .getServiceFor(provider)
                .createCustomer(paymentRequestExternalId, payer, sortCode, accountNumber);

    }

    public Payer create(Long accountId, String paymentRequestExternalId, Map<String, String> createPayerMap) {
        Transaction transaction = transactionService.receiveDirectDebitDetailsFor(accountId, paymentRequestExternalId);
        Payer payer = payerParser.parse(createPayerMap, transaction);
        Long id = payerDao.insert(payer);
        payer.setId(id);

        LOGGER.info("Created Payer with external id {}", payer.getExternalId());
        transactionService.payerCreatedFor(transaction);
        return payer;
    }
}
