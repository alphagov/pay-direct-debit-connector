package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
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
import javax.ws.rs.NotFoundException;
import java.util.Map;
import java.util.Optional;

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
        ConfirmationDetails confirmationDetails = initialiseConfirmationDetails(mandateExternalId, gatewayAccount, confirmDetailsRequest);

        //mandateConfirmService.checkConfirmable(mandate);
        
        GoCardlessConfirmationDetails gcConfirmationDetails = initialiseGoCardlessConfirmationDetails(confirmationDetails);
        
        insertPlaceHolders(gcConfirmationDetails);
        sendToGoCardless(confirmationDetails, gcConfirmationDetails);
        completeConfirmation(gcConfirmationDetails);
    }
    
    private ConfirmationDetails initialiseConfirmationDetails(
            String mandateExternalId, 
            GatewayAccount gatewayAccount,
            Map<String, 
            String> confirmDetailsRequest
    ) {
        String sortCode = confirmDetailsRequest.get("sort_code");
        String accountNumber = confirmDetailsRequest.get("account_number");
        
        Mandate mandate = mandateDao.findByExternalId(mandateExternalId)
                .orElseThrow(NotFoundException::new);
        
        Transaction transaction = Optional
                .ofNullable(confirmDetailsRequest.get("transaction_external_id"))
                .map(transactionExternalId -> transactionService
                        .findTransactionForExternalIdAndGatewayAccountExternalId(
                                transactionExternalId, 
                                gatewayAccount.getExternalId()
                        ))
                .orElse(null);
        
        return new ConfirmationDetails(mandate, transaction, accountNumber, sortCode);
    }
    
    private GoCardlessConfirmationDetails initialiseGoCardlessConfirmationDetails(ConfirmationDetails confirmationDetails) {
        Mandate mandate = confirmationDetails.getMandate();
        Transaction transaction = confirmationDetails.getTransaction();
        
        GoCardlessMandate goCardlessMandate = new GoCardlessMandate(mandate.getId());
        GoCardlessCustomer gcCustomer = new GoCardlessCustomer(mandate.getPayer().getId());
        
        if (mandate.isOneOff()) {
            return new GoCardlessConfirmationDetails(
                    goCardlessMandate,
                    gcCustomer,
                    new GoCardlessPayment(transaction.getId())
            );
        } else {
            return new GoCardlessConfirmationDetails(
                    goCardlessMandate,
                    gcCustomer
            );
        }
    }
    
    @org.jdbi.v3.sqlobject.transaction.Transaction
    private void insertPlaceHolders(GoCardlessConfirmationDetails confirmationDetails) {
        Long goCardlessCustomerPrimaryKey = goCardlessCustomerDao.insert(confirmationDetails.getCustomer());
        confirmationDetails.getCustomer().setId(goCardlessCustomerPrimaryKey);
        
        Long goCardlessMandatePrimaryKey = goCardlessMandateDao.insert(confirmationDetails.getMandate());
        confirmationDetails.getMandate().setId(goCardlessMandatePrimaryKey);
        
        if (confirmationDetails.getPayment() != null) {
            Long goCardlessPaymentPrimaryKey = goCardlessPaymentDao.insert(confirmationDetails.getPayment());
            confirmationDetails.getPayment().setId(goCardlessPaymentPrimaryKey);
        }
    }
    
    private void sendToGoCardless(ConfirmationDetails confirmationDetails, GoCardlessConfirmationDetails goCardlessConfirmationDetails) {
        LOGGER.info("Confirming direct debit details, mandate with id: {}", confirmationDetails.getMandate().getExternalId());
        
        Mandate mandate = confirmationDetails.getMandate();
        Payer payer = mandate.getPayer();
        String goCardlessCustomerId = sendCustomer(
                mandate.getExternalId(),
                payer);
        goCardlessConfirmationDetails.getCustomer().setCustomerId(goCardlessCustomerId);
        
        String goCardlessCustimerBankAccountId = createCustomerBankAccount(
                mandate.getExternalId(),
                goCardlessConfirmationDetails.getCustomer(),
                payer,
                payer.getSortCode(),
                payer.getAccountNumber()
        );
        goCardlessConfirmationDetails.getCustomer().setCustomerBankAccountId(goCardlessCustimerBankAccountId);


        String goCardlessMandateId = createMandate(mandate, goCardlessConfirmationDetails.getCustomer());
        goCardlessConfirmationDetails.getMandate().setGoCardlessMandateId(goCardlessMandateId);

        if (confirmationDetails.getMandate().isOneOff()) {
            Transaction transaction = confirmationDetails.getTransaction();
            String goCardlessPaymentId = createPayment(transaction, goCardlessConfirmationDetails.getMandate());
            goCardlessConfirmationDetails.getPayment().setPaymentId(goCardlessPaymentId);
        }
    }

    @org.jdbi.v3.sqlobject.transaction.Transaction
    private void completeConfirmation(GoCardlessConfirmationDetails confirmationDetails) {
        //goCardlessPaymentDao.update(confirmationDetails.getPayment());
        goCardlessMandateDao.update(confirmationDetails.getMandate());
        goCardlessCustomerDao.update(confirmationDetails.getCustomer());
        
        //@TODO write events and update state

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

    
    private String sendCustomer(String mandateExternalId, Payer payer) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer, mandate id: {}", mandateExternalId);

            String goCardlessCustomerId = goCardlessClientFacade.createCustomer(mandateExternalId, payer);
            LOGGER.info("Created customer in gocardless, mandate id: {}", mandateExternalId);

            return goCardlessCustomerId;
        } catch (Exception exc) {
            logException(exc, "customer", mandateExternalId);
            throw new CreateCustomerFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private String createCustomerBankAccount(String mandateExternalId, GoCardlessCustomer goCardlessCustomer, Payer payer, String sortCode, String accountNumber) {
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, mandate id: {}", mandateExternalId);

            String customerBankAccountId = goCardlessClientFacade.createCustomerBankAccount(
                    mandateExternalId,
                    goCardlessCustomer,
                    payer.getName(),
                    sortCode,
                    accountNumber);

            LOGGER.info("Created customer bank account in gocardless, mandate id: {}", mandateExternalId);

            return customerBankAccountId;
        } catch (Exception exc) {
            logException(exc, "bank account", mandateExternalId);
            throw new CreateCustomerBankAccountFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private String createMandate(Mandate mandate, GoCardlessCustomer goCardlessCustomer) {
        try {

            LOGGER.info("Attempting to call gocardless to create a mandate, pay mandate id: {}", mandate.getExternalId());

            GoCardlessMandate goCardlessMandate = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);
            LOGGER.info("Created mandate in gocardless, pay mandate id: {}, gocardless mandate id: {}", 
                    mandate.getExternalId(),
                    goCardlessMandate.getGoCardlessMandateId());

            return goCardlessMandate.getGoCardlessMandateId();
        } catch (Exception exc) {
            logException(exc, "mandate", mandate.getExternalId());
            throw new CreateMandateFailedException(mandate.getExternalId());
        }
    }

    private String createPayment(Transaction transaction, GoCardlessMandate goCardlessMandate) {
        try {
            LOGGER.info("Attempting to call gocardless to create a payment, mandate id: {}, transaction id: {}", 
                    transaction.getMandate().getExternalId(), 
                    transaction.getExternalId());

            GoCardlessPayment goCardlessPayment = goCardlessClientFacade.createPayment(transaction, goCardlessMandate);

            LOGGER.info("Created payment in gocardless, mandate id: {}, transaction id: {}, gocardless payment id: {}", 
                    transaction.getMandate().getExternalId(), 
                    transaction.getExternalId(), 
                    goCardlessPayment.getPaymentId());
            
            return goCardlessPayment.getPaymentId();
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
