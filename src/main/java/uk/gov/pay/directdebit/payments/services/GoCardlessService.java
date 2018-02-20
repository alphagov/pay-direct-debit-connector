package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

public class GoCardlessService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountResource.class);

    private GoCardlessClientWrapper goCardlessClientWrapper;
    private GoCardlessCustomerDao goCardlessCustomerDao;

    public GoCardlessService(GoCardlessClientWrapper goCardlessClientWrapper, GoCardlessCustomerDao goCardlessCustomerDao) {
        this.goCardlessClientWrapper = goCardlessClientWrapper;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
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


    private String createCustomerBankAccount(String paymentRequestExternalId, GoCardlessCustomer goCardlessCustomer, Payer payer, String sortCode, String accountId) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, payment request id: {}", paymentRequestExternalId);

            GoCardlessCustomer customerWithBankAccount = goCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), sortCode, accountId);

            LOGGER.info("Created customer bank account in gocardless, payment request id: {}", paymentRequestExternalId);

            goCardlessCustomerDao.updateBankAccountId(customerWithBankAccount.getId(), customerWithBankAccount.getCustomerBankAccountId());
            return customerWithBankAccount.getCustomerId();

        } catch (Exception exc) {
            LOGGER.error("Failed to create a customer bank account in gocardless, payment request id: {}, error: {}", paymentRequestExternalId, exc.getMessage());
            throw new CreateCustomerBankAccountFailedException(paymentRequestExternalId, payer.getExternalId());
        }
    }

    @Override
    public String createCustomer(String paymentRequestExternalId, Payer payer, String sortCode, String accountNumber) {
        GoCardlessCustomer customer = createCustomer(paymentRequestExternalId, payer);
        return createCustomerBankAccount(paymentRequestExternalId, customer, payer, sortCode, accountNumber);
    }
}
