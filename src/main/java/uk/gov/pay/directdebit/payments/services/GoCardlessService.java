package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

import java.util.Map;

public class GoCardlessService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessService.class);

    private final PayerService payerService;
    private final GoCardlessClientWrapper goCardlessClientWrapper;
    private final GoCardlessCustomerDao goCardlessCustomerDao;

    public GoCardlessService(PayerService payerService,
                             GoCardlessClientWrapper goCardlessClientWrapper,
                             GoCardlessCustomerDao goCardlessCustomerDao) {
        this.payerService = payerService;
        this.goCardlessClientWrapper = goCardlessClientWrapper;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
    }

    @Override
    public Payer createPayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        Payer payer = payerService.create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
        GoCardlessCustomer customer = createCustomer(paymentRequestExternalId, payer);
        createCustomerBankAccount(paymentRequestExternalId, customer, payer, createPayerRequest);
        return payer;
    }

    private GoCardlessCustomer createCustomer(String paymentRequestExternalId, Payer payer) {
        try {

            LOGGER.info("Attempting to call gocardless to create a customer, payment request id: {}", paymentRequestExternalId);

            GoCardlessCustomer customer = goCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer);
            LOGGER.info("Created customer in gocardless, payment request id: {}", paymentRequestExternalId);

            Long id = goCardlessCustomerDao.insert(customer);
            customer.setId(id);
            return customer;

        } catch (Exception exc) {
            LOGGER.error("Failed to create a customer in gocardless, payment request id: {}, error: {}", paymentRequestExternalId, exc.getMessage());
            throw new CreateCustomerFailedException(paymentRequestExternalId, payer.getExternalId());
        }
    }


    private String createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer goCardlessCustomer, Payer payer, Map<String, String> createPayerRequest) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, payment request id: {}", paymentRequestExternalId);

            String sortCode = createPayerRequest.get("sort_code");
            String accountNumber = createPayerRequest.get("account_number");

            GoCardlessCustomer customerWithBankAccount = goCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), sortCode, accountNumber);

            LOGGER.info("Created customer bank account in gocardless, payment request id: {}", paymentRequestExternalId);

            goCardlessCustomerDao.updateBankAccountId(customerWithBankAccount.getId(), customerWithBankAccount.getCustomerBankAccountId());
            return customerWithBankAccount.getCustomerId();

        } catch (Exception exc) {
            LOGGER.error("Failed to create a customer bank account in gocardless, payment request id: {}, error: {}", paymentRequestExternalId, exc.getMessage());
            throw new CreateCustomerBankAccountFailedException(paymentRequestExternalId, payer.getExternalId());
        }
    }
}
