package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.CustomerNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Map;

public class GoCardlessService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessService.class);

    private final PayerService payerService;
    private final PaymentConfirmService paymentConfirmService;
    private final GoCardlessClientWrapper goCardlessClientWrapper;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    public GoCardlessService(PayerService payerService,
                             PaymentConfirmService paymentConfirmService,
                             GoCardlessClientWrapper goCardlessClientWrapper,
                             GoCardlessCustomerDao goCardlessCustomerDao,
                             GoCardlessPaymentDao goCardlessPaymentDao,
                             GoCardlessMandateDao goCardlessMandateDao) {
        this.payerService = payerService;
        this.paymentConfirmService = paymentConfirmService;
        this.goCardlessClientWrapper = goCardlessClientWrapper;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
    }

    @Override
    public Payer createPayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        Payer payer = payerService.create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
        GoCardlessCustomer customer = createCustomer(paymentRequestExternalId, payer);
        createCustomerBankAccount(paymentRequestExternalId, customer, payer, createPayerRequest);
        return payer;
    }

    @Override
    public void confirm(String paymentRequestExternalId, GatewayAccount gatewayAccount) {
        PaymentConfirmService.ConfirmationDetails confirmationDetails = paymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId);
        GoCardlessMandate goCardlessMandate = createMandate(paymentRequestExternalId, confirmationDetails.getMandate());
        createPayment(paymentRequestExternalId, confirmationDetails.getTransaction(), goCardlessMandate);
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

    private GoCardlessMandate createMandate(String paymentRequestExternalId, Mandate payMandate) {
        GoCardlessCustomer goCardlessCustomer = goCardlessCustomerDao
                .findByPayerId(payMandate.getPayerId())
                .orElseThrow(() -> new CustomerNotFoundException(paymentRequestExternalId, payMandate.getExternalId()));
        try {

            LOGGER.info("Attempting to call gocardless to create a mandate, payment request id: {}", paymentRequestExternalId);

            GoCardlessMandate mandate = goCardlessClientWrapper.createMandate(paymentRequestExternalId, payMandate, goCardlessCustomer);
            LOGGER.info("Created mandate in gocardless, payment request id: {}", paymentRequestExternalId);

            Long id = goCardlessMandateDao.insert(mandate);
            mandate.setId(id);
            return mandate;

        } catch (Exception exc) {
            LOGGER.error("Failed to create a mandate in gocardless, payment request id: {}, error: {}", paymentRequestExternalId, exc.getMessage());
            throw new CreateMandateFailedException(paymentRequestExternalId, payMandate.getExternalId());
        }
    }

    private String createPayment(String paymentRequestExternalId, Transaction transaction, GoCardlessMandate goCardlessMandate) {
        try {
            LOGGER.info("Attempting to call gocardless to create a payment, payment request id: {}", paymentRequestExternalId);

            GoCardlessPayment goCardlessPayment = goCardlessClientWrapper.createPayment(paymentRequestExternalId, goCardlessMandate, transaction);

            LOGGER.info("Created payment in gocardless, payment request id: {}", paymentRequestExternalId);

            return goCardlessPaymentDao.insert(goCardlessPayment).toString();

        } catch (Exception exc) {
            LOGGER.error("Failed to create a customer bank account in gocardless, payment request id: {}, error: {}", paymentRequestExternalId, exc.getMessage());
            throw new CreatePaymentFailedException(paymentRequestExternalId);
        }
    }
}
