package uk.gov.pay.directdebit.payments.services;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateConfirmService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
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
    private final MandateConfirmService mandateConfirmService;
    private final GoCardlessClientFacade goCardlessClientFacade;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final GoCardlessEventDao goCardlessEventDao;
    private final MandateDao mandateDao;
    private final BankAccountDetailsParser bankAccountDetailsParser;

    @Inject
    public GoCardlessService(PayerService payerService,
            TransactionService transactionService, MandateConfirmService mandateConfirmService,
            GoCardlessClientFacade goCardlessClientFacade,
            GoCardlessCustomerDao goCardlessCustomerDao,
            GoCardlessPaymentDao goCardlessPaymentDao,
            GoCardlessMandateDao goCardlessMandateDao,
            GoCardlessEventDao goCardlessEventDao,
            MandateDao mandateDao,
            BankAccountDetailsParser bankAccountDetailsParser) {
        this.payerService = payerService;
        this.transactionService = transactionService;
        this.mandateConfirmService = mandateConfirmService;
        this.goCardlessClientFacade = goCardlessClientFacade;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
        this.goCardlessEventDao = goCardlessEventDao;
        this.mandateDao = mandateDao;
        this.bankAccountDetailsParser = bankAccountDetailsParser;
    }

    @Override
    public Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        return payerService.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
    }

    @Override
    public void confirm(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest) {
        ConfirmationDetails confirmationDetails = mandateConfirmService
                .confirm(mandateExternalId, confirmDetailsRequest);
        Mandate mandate = confirmationDetails.getMandate();

        LOGGER.info("Confirming direct debit details, mandate with id: {}", mandateExternalId);
        GoCardlessCustomer customer = createCustomer(mandateExternalId, mandate.getPayer());
        createCustomerBankAccount(mandateExternalId, customer, mandate.getPayer(), confirmationDetails.getSortCode(), confirmationDetails.getAccountNumber());
        GoCardlessMandate goCardlessMandate = createMandate(mandate, customer);

        if (mandate.getType().equals(MandateType.ONE_OFF)) {
            Transaction transaction = confirmationDetails.getTransaction();
            GoCardlessPayment payment = createPayment(transaction, goCardlessMandate);
            transactionService.paymentSubmittedToProviderFor(transaction, payment.getChargeDate());
        }
    }

    @Override
    public Transaction collect(GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest) {
        throw new NotImplementedException("Collecting payments from an existing mandate via GoCardless is not yet supported");
    }

    @Override
    public BankAccountValidationResponse validate(String mandateExternalId, Map<String, String> bankAccountDetailsRequest) {
        BankAccountDetails bankAccountDetails = bankAccountDetailsParser.parse(bankAccountDetailsRequest);
        LOGGER.info("Attempting to call gocardless to validate a bank account, mandate id: {}", mandateExternalId);
        try {
            GoCardlessBankAccountLookup lookup = goCardlessClientFacade.validate(bankAccountDetails);
            return new BankAccountValidationResponse(lookup.isBacs(), lookup.getBankName());
        } catch (Exception exc) {
            LOGGER.warn("Exception while validating bank account details in gocardless, message: {}", exc.getMessage());
            return new BankAccountValidationResponse(false);
        }
    }

    
    private GoCardlessCustomer createCustomer(String mandateExternalId, Payer payer) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer, mandate id: {}", mandateExternalId);

            GoCardlessCustomer customer = goCardlessClientFacade.createCustomer(mandateExternalId, payer);
            LOGGER.info("Created customer in gocardless, mandate id: {}", mandateExternalId);

            Long id = goCardlessCustomerDao.insert(customer);
            customer.setId(id);
            return customer;
        } catch (Exception exc) {
            logException(exc, "customer", mandateExternalId);
            throw new CreateCustomerFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private void createCustomerBankAccount(String mandateExternalId, GoCardlessCustomer goCardlessCustomer, Payer payer, String sortCode, String accountNumber) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, mandate id: {}", mandateExternalId);

            GoCardlessCustomer customerWithBankAccount = goCardlessClientFacade.createCustomerBankAccount(
                    mandateExternalId,
                    goCardlessCustomer,
                    payer.getName(),
                    sortCode,
                    accountNumber);

            LOGGER.info("Created customer bank account in gocardless, mandate id: {}", mandateExternalId);

            goCardlessCustomerDao.updateBankAccountId(customerWithBankAccount.getId(), customerWithBankAccount.getCustomerBankAccountId());
        } catch (Exception exc) {
            logException(exc, "bank account", mandateExternalId);
            throw new CreateCustomerBankAccountFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private GoCardlessMandate createMandate(Mandate mandate, GoCardlessCustomer goCardlessCustomer) {
        try {

            LOGGER.info("Attempting to call gocardless to create a mandate, pay mandate id: {}", mandate.getExternalId());

            GoCardlessMandate goCardlessMandate = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);
            LOGGER.info("Created mandate in gocardless, pay mandate id: {}, gocardless mandate id: {}", 
                    mandate.getExternalId(),
                    goCardlessMandate.getGoCardlessMandateId());

            Long id = goCardlessMandateDao.insert(goCardlessMandate);
            goCardlessMandate.setId(id);
            mandateDao.updateMandateReference(mandate.getId(), goCardlessMandate.getGoCardlessReference());
            return goCardlessMandate;
        } catch (Exception exc) {
            logException(exc, "mandate", mandate.getExternalId());
            throw new CreateMandateFailedException(mandate.getExternalId());
        }
    }

    private GoCardlessPayment createPayment(Transaction transaction, GoCardlessMandate goCardlessMandate) {
        try {
            LOGGER.info("Attempting to call gocardless to create a payment, mandate id: {}, transaction id: {}", 
                    transaction.getMandate().getExternalId(), 
                    transaction.getExternalId());

            GoCardlessPayment goCardlessPayment = goCardlessClientFacade.createPayment(transaction, goCardlessMandate);

            LOGGER.info("Created payment in gocardless, mandate id: {}, transaction id: {}, gocardless payment id: {}", 
                    transaction.getMandate().getExternalId(), 
                    transaction.getExternalId(), 
                    goCardlessPayment.getPaymentId());
            
            Long id = goCardlessPaymentDao.insert(goCardlessPayment);
            goCardlessPayment.setId(id);
            return goCardlessPayment;
        } catch (Exception exc) {
            logException(exc, "payment", transaction.getExternalId());
            throw new CreatePaymentFailedException(transaction.getMandate().getExternalId(), transaction.getExternalId());
        }
    }

    public void storeEvent(GoCardlessEvent event) {
        goCardlessEventDao.insert(event);
        LOGGER.info("Inserted gocardless event with gocardless event id {} ", event.getGoCardlessEventId());
    }

    public void updateInternalEventId(GoCardlessEvent event) {
        goCardlessEventDao.updateEventId(event.getId(), event.getEventId());
        LOGGER.info("Updated gocardless event with gocardless event id {}, internal event id {} ", event.getGoCardlessEventId(), event.getEventId());
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
    private void logException(Exception exc, String resource, String mandateExternalId) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in gocardless, mandate id : {}, error: {}, cause: {}", resource, mandateExternalId, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in gocardless, mandate id: {}, error: {}", resource, mandateExternalId, exc.getMessage());
        }
    }
    
}
