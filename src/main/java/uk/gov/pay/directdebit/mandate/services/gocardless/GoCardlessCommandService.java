package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.OneOffMandateConfirmationDetails;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvideCommandService;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;

import javax.inject.Inject;

public class GoCardlessCommandService implements DirectDebitPaymentProvideCommandService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GoCardlessService.class);

    private final GoCardlessClientFacade goCardlessClientFacade;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;

    @Inject
    public GoCardlessCommandService(
            GoCardlessClientFacade goCardlessClientFacade,
            GoCardlessCustomerDao goCardlessCustomerDao,
            GoCardlessPaymentDao goCardlessPaymentDao,
            GoCardlessMandateDao goCardlessMandateDao) {
        this.goCardlessClientFacade = goCardlessClientFacade;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
    }

    private void persist(GoCardlessCustomer customer) {
        goCardlessCustomerDao.insert(customer);
    }

    private void persist(GoCardlessMandate mandate) {
        goCardlessMandateDao.insert(mandate);
    }

    private void persist(GoCardlessPayment payment) {
        goCardlessPaymentDao.insert(payment);
    }

    private GoCardlessCustomer createCustomer(Mandate mandate) {
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

    private GoCardlessCustomer createCustomerBankAccount(Mandate mandate, GoCardlessCustomer goCardlessCustomer, BankAccountDetails bankAccountDetails) {
        String mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer();
        
        try {
            LOGGER.info("Attempting to call gocardless to create a customer bank account, mandate id: {}", mandateExternalId);

            GoCardlessCustomer customerWithBankAccount = goCardlessClientFacade.createCustomerBankAccount(
                    mandateExternalId,
                    goCardlessCustomer,
                    payer.getName(),
                    bankAccountDetails.getSortCode(),
                    bankAccountDetails.getAccountNumber()
            );

            LOGGER.info("Created customer bank account in gocardless, mandate id: {}", mandateExternalId);

            return customerWithBankAccount;
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

            return goCardlessPayment;
        } catch (Exception exc) {
            logException(exc, "payment", transaction.getExternalId());
            throw new CreatePaymentFailedException(transaction.getMandate().getExternalId(), transaction.getExternalId());
        }
    }

    public void confirmMandate(MandateConfirmationDetails mandateConfirmationDetails) {
        Mandate mandate = mandateConfirmationDetails.getMandate();
        LOGGER.info("Confirming direct debit details, mandate with id: {}", mandate.getExternalId());
        GoCardlessCustomer goCardlessCustomer = createCustomer(mandate);
        GoCardlessCustomer goCardlessCustomerWithBankAccount = createCustomerBankAccount(
                mandate,
                goCardlessCustomer,
                mandateConfirmationDetails.getBankAccountDetails()
        );
        GoCardlessMandate goCardlessMandate = createMandate(mandate, goCardlessCustomerWithBankAccount);

        persist(goCardlessCustomer);
        persist(goCardlessMandate);
    }

    public void confirmMandate(OneOffMandateConfirmationDetails mandateConfirmationDetails) {
        Mandate mandate = mandateConfirmationDetails.getMandate();
        LOGGER.info("Confirming direct debit details, mandate with id: {}", mandate.getExternalId());
        GoCardlessCustomer goCardlessCustomer = createCustomer(mandate);
        GoCardlessCustomer goCardlessCustomerWithBankAccount = createCustomerBankAccount(
                mandate,
                goCardlessCustomer,
                mandateConfirmationDetails.getBankAccountDetails()
        );
        GoCardlessMandate goCardlessMandate = createMandate(mandate, goCardlessCustomerWithBankAccount);
        GoCardlessPayment goCardlessPayment = createPayment(mandateConfirmationDetails.getTransaction(), goCardlessMandate);

        persist(goCardlessCustomer);
        persist(goCardlessMandate);
        persist(goCardlessPayment);
    }

    private void logException(Exception exc, String resource, String id) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in gocardless, id : {}, error: {}, cause: {}", resource, id, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in gocardless, id: {}, error: {}", resource, id, exc.getMessage());
        }
    }
}
