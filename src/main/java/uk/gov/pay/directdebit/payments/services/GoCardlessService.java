package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.CustomerNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.util.Map;

public class GoCardlessService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessService.class);

    private final PayerService payerService;
    private final TransactionService transactionService;
    private final PaymentConfirmService paymentConfirmService;
    private final GoCardlessClientWrapper goCardlessClientWrapper;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final GoCardlessEventDao goCardlessEventDao;

    @Inject
    public GoCardlessService(PayerService payerService,
                             TransactionService transactionService, PaymentConfirmService paymentConfirmService,
                             GoCardlessClientWrapper goCardlessClientWrapper,
                             GoCardlessCustomerDao goCardlessCustomerDao,
                             GoCardlessPaymentDao goCardlessPaymentDao,
                             GoCardlessMandateDao goCardlessMandateDao,
                             GoCardlessEventDao goCardlessEventDao) {
        this.payerService = payerService;
        this.transactionService = transactionService;
        this.paymentConfirmService = paymentConfirmService;
        this.goCardlessClientWrapper = goCardlessClientWrapper;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
        this.goCardlessEventDao = goCardlessEventDao;
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
        ConfirmationDetails confirmationDetails = paymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId);
        GoCardlessMandate goCardlessMandate = createMandate(paymentRequestExternalId, confirmationDetails.getMandate());
        createPayment(paymentRequestExternalId, confirmationDetails.getTransaction(), goCardlessMandate);
        transactionService.paymentCreatedFor(confirmationDetails.getTransaction());
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
            logException(exc, "customer", paymentRequestExternalId);
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
            logException(exc, "bank account", paymentRequestExternalId);
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
            logException(exc, "mandate", paymentRequestExternalId);
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
            logException(exc, "payment", paymentRequestExternalId);
            throw new CreatePaymentFailedException(paymentRequestExternalId);
        }
    }

    public void storeEvent(GoCardlessEvent event) {
        goCardlessEventDao.insert(event);
        LOGGER.info("inserted gocardless event with id {} ", event.getEventId());
    }

    public GoCardlessPayment findPaymentForEvent(GoCardlessEvent event) {
        return goCardlessPaymentDao
                .findByEventResourceId(event.getResourceId())
                .orElseThrow(() -> {
                     LOGGER.error("Couldn't find gocardless payment for event: {}", event.getJson());
                     return new GoCardlessPaymentNotFoundException(event.getResourceId());
                });
    }

    public GoCardlessMandate findMandateForEvent(GoCardlessEvent event) {
       return goCardlessMandateDao
                .findByEventResourceId(event.getResourceId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for event: {}", event.getJson());
                    return new GoCardlessMandateNotFoundException(event.getResourceId());
                });
    }
    private void logException(Exception exc, String resource, String paymentRequestExternalId) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in gocardless, payment request id: {}, error: {}, cause: {}", resource, paymentRequestExternalId, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in gocardless, payment request id: {}, error: {}", resource, paymentRequestExternalId, exc.getMessage());
        }
    }
}
