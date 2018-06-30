package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.util.Map;

public class GoCardlessService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessService.class);

    private final GoCardlessClientFacade goCardlessClientFacade;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessEventDao goCardlessEventDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final MandateDao mandateDao;
    private final BankAccountDetailsParser bankAccountDetailsParser;

    @Inject
    public GoCardlessService(
            GoCardlessClientFacade goCardlessClientFacade,
            GoCardlessCustomerDao goCardlessCustomerDao,
            GoCardlessMandateDao goCardlessMandateDao,
            GoCardlessEventDao goCardlessEventDao,
            GoCardlessPaymentDao goCardlessPaymentDao,
            BankAccountDetailsParser bankAccountDetailsParser,
            MandateDao mandateDao) {
        this.goCardlessClientFacade = goCardlessClientFacade;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessEventDao = goCardlessEventDao;
        this.mandateDao = mandateDao;
        this.bankAccountDetailsParser = bankAccountDetailsParser;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
    }

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
    
    GoCardlessCustomer createCustomer(Mandate mandate) {
        String mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer();
        try {
            LOGGER.info("Attempting to call gocardless to create a customer, mandate id: {}", mandateExternalId);

            GoCardlessCustomer customer = goCardlessClientFacade.createCustomer(mandateExternalId, payer);
            LOGGER.info("Created customer in gocardless, mandate id: {}", mandateExternalId);

            return customer;
        } catch (Exception exc) {
            logException(exc, "customer", mandateExternalId);
            throw new CreateCustomerFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    String createCustomerBankAccount(Mandate mandate, GoCardlessCustomer customer, BankAccountDetails accountDetails) {
        String mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer();
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, mandate id: {}", mandateExternalId);

            GoCardlessCustomer customerWithBankAccount = goCardlessClientFacade.createCustomerBankAccount(
                    mandate.getExternalId(),
                    customer,
                    payer.getName(),
                    accountDetails.getSortCode(),
                    accountDetails.getAccountNumber()
            );

            LOGGER.info("Created customer bank account in gocardless, mandate id: {}", mandateExternalId);

            return customerWithBankAccount.getCustomerBankAccountId();
        } catch (Exception exc) {
            logException(exc, "bank account", mandateExternalId);
            throw new CreateCustomerBankAccountFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    GoCardlessMandate createMandate(Mandate mandate, GoCardlessCustomer goCardlessCustomer) {
        try {

            LOGGER.info("Attempting to call gocardless to create a mandate, pay mandate id: {}", mandate.getExternalId());

            GoCardlessMandate goCardlessMandate = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);
            LOGGER.info("Created mandate in gocardless, pay mandate id: {}, gocardless mandate id: {}", 
                    mandate.getExternalId(),
                    goCardlessMandate.getGoCardlessMandateId());

            return goCardlessMandate;
        } catch (Exception exc) {
            logException(exc, "mandate", mandate.getExternalId());
            throw new CreateMandateFailedException(mandate.getExternalId());
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

    public GoCardlessMandate findGoCardlessMandateForEvent(GoCardlessEvent event) {
        return goCardlessMandateDao
                .findByEventResourceId(event.getResourceId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for event: {}", event.getJson());
                    return new GoCardlessMandateNotFoundException("resource id", event.getResourceId());
                });
    }

    private void logException(Exception exc, String resource, String id) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in gocardless, id : {}, error: {}, cause: {}", resource, id, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in gocardless, id: {}, error: {}", resource, id, exc.getMessage());
        }
    }

    GoCardlessMandate findGoCardlessMandateForMandate(Mandate mandate) {
        return goCardlessMandateDao
                .findByMandateId(mandate.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for mandate with id: {}", mandate.getExternalId());
                    return new GoCardlessMandateNotFoundException("mandate id", mandate.getExternalId());
                });
    }

    GoCardlessPayment createPayment(Transaction transaction, GoCardlessMandate goCardlessMandate) {
        try {
            LOGGER.info("Attempting to call gocardless to create a payment, mandate id: {}, transaction id: {}",
                    transaction.getMandate().getExternalId(),
                    transaction.getExternalId());

            GoCardlessPayment goCardlessPayment = goCardlessClientFacade.createPayment(transaction, goCardlessMandate);

            LOGGER.info("Created payment in gocardless, mandate id: {}, transaction id: {}, gocardless payment id: {}",
                    transaction.getMandate().getExternalId(),
                    transaction.getExternalId(),
                    goCardlessPayment.getPaymentId());

            return goCardlessPayment;
        } catch (Exception exc) {
            logException(exc, "payment", transaction.getExternalId());
            throw new CreatePaymentFailedException(transaction.getMandate().getExternalId(), transaction.getExternalId());
        }
    }

    Long persistGoCardlessMandate(GoCardlessMandate goCardlessMandate) {
        return goCardlessMandateDao.insert(goCardlessMandate);
    }

    Long persistGoCardlessCustomer(GoCardlessCustomer goCardlessCustomer) {
        return goCardlessCustomerDao.insert(goCardlessCustomer);
    }

    void updateMandateReference(Mandate mandate, String goCardlessReference) {
        mandateDao.updateMandateReference(mandate.getId(), goCardlessReference);
    }

    public void persistGoCardlessPayment(GoCardlessPayment payment) {
        goCardlessPaymentDao.insert(payment);
    }
}
