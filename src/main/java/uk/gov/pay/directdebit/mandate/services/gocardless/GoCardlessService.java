package uk.gov.pay.directdebit.mandate.services.gocardless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFactory;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProviderCommandService;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class GoCardlessService implements DirectDebitPaymentProviderCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoCardlessService.class);

    private final GoCardlessClientFactory goCardlessClientFactory;
    private final GoCardlessCustomerDao goCardlessCustomerDao;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;

    @Inject
    public GoCardlessService(
            GoCardlessClientFactory goCardlessClientFactory,
            GoCardlessCustomerDao goCardlessCustomerDao,
            GoCardlessPaymentDao goCardlessPaymentDao,
            GoCardlessMandateDao goCardlessMandateDao) {
        this.goCardlessClientFactory = goCardlessClientFactory;
        this.goCardlessCustomerDao = goCardlessCustomerDao;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
    }

    @Override
    public Mandate confirmOnDemandMandate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Confirming direct debit details, on demand mandate with id: {}", mandate.getExternalId());
        GoCardlessCustomer goCardlessCustomer = createCustomer(mandate);
        GoCardlessCustomer goCardlessCustomerWithBankAccount = createCustomerBankAccount(
                mandate,
                goCardlessCustomer,
                bankAccountDetails
        );
        GoCardlessMandate goCardlessMandate = createMandate(mandate, goCardlessCustomerWithBankAccount);
        mandate.setMandateReference(goCardlessMandate.getGoCardlessReference());
        persist(goCardlessCustomer);
        persist(goCardlessMandate);
        return mandate;
    }

    @Override
    public OneOffConfirmationDetails confirmOneOffMandate(Mandate mandate, BankAccountDetails bankAccountDetails, Transaction transaction) {
        LOGGER.info("Confirming direct debit details, one off mandate with id: {}", mandate.getExternalId());
        GoCardlessCustomer goCardlessCustomer = createCustomer(mandate);
        GoCardlessCustomer goCardlessCustomerWithBankAccount = createCustomerBankAccount(
                mandate,
                goCardlessCustomer,
                bankAccountDetails
        );
        GoCardlessMandate goCardlessMandate = createMandate(mandate, goCardlessCustomerWithBankAccount);
        mandate.setMandateReference(goCardlessMandate.getGoCardlessReference());
        transaction.setMandate(mandate);
        GoCardlessPayment goCardlessPayment = createPayment(transaction, goCardlessMandate);

        persist(goCardlessCustomer);
        persist(goCardlessMandate);
        persist(goCardlessPayment);
        return new OneOffConfirmationDetails(mandate, goCardlessPayment.getChargeDate());
    }

    @Override
    public LocalDate collect(Mandate mandate, Transaction transaction) {
        LOGGER.info("Collecting payment for GoCardless, mandate with id: {}, transaction with id: {}", mandate.getExternalId(), transaction.getExternalId());

        GoCardlessMandate goCardlessMandate = findGoCardlessMandateForMandate(mandate);
        GoCardlessPayment goCardlessPayment = createPayment(transaction, goCardlessMandate);

        persist(goCardlessPayment);
        return goCardlessPayment.getChargeDate();
    }

    @Override
    public BankAccountValidationResponse validate(Mandate mandate, BankAccountDetails bankAccountDetails) {
        LOGGER.info("Attempting to call GoCardless to validate a bank account, mandate with id: {}", mandate.getExternalId());
        try {
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessBankAccountLookup lookup = goCardlessClientFacade.validate(bankAccountDetails);
            return new BankAccountValidationResponse(lookup.isBacs(), lookup.getBankName());
        } catch (Exception exc) {
            LOGGER.error("Exception while validating bank account details in GoCardless, message: {}", exc.getMessage());
            return new BankAccountValidationResponse(false);
        }
    }

    @Override
    public Optional<SunName> getSunName(Mandate mandate) {
        LOGGER.info("Attempting to call GoCardless to retrieve service user name from creditor for mandate with id: {}", mandate.getExternalId());
        try {
            return goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken()).getSunName();
        } catch (Exception exc) {
            LOGGER.error("Exception while retrieving service user name from GoCardless, message: {}", exc.getMessage());
            return Optional.empty();
        }
    }

    private GoCardlessCustomer createCustomer(Mandate mandate) {
        MandateExternalId mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer();
        try {
            LOGGER.info("Attempting to call GoCardless to create a customer, mandate id: {}", mandateExternalId);
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessCustomer customer = goCardlessClientFacade.createCustomer(mandateExternalId, payer);
            LOGGER.info("Created customer in GoCardless, mandate id: {}", mandateExternalId);

            return customer;
        } catch (Exception exc) {
            logException(exc, "customer", mandateExternalId.toString());
            throw new CreateCustomerFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private GoCardlessCustomer createCustomerBankAccount(Mandate mandate, GoCardlessCustomer goCardlessCustomer, BankAccountDetails bankAccountDetails) {
        MandateExternalId mandateExternalId = mandate.getExternalId();
        Payer payer = mandate.getPayer();

        try {
            LOGGER.info("Attempting to call GoCardless to create a customer bank account, mandate id: {}", mandateExternalId);
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());
            GoCardlessCustomer customerWithBankAccount = goCardlessClientFacade.createCustomerBankAccount(
                    mandateExternalId,
                    goCardlessCustomer,
                    payer.getName(),
                    bankAccountDetails.getSortCode(),
                    bankAccountDetails.getAccountNumber()
            );

            LOGGER.info("Created customer bank account in GoCardless, mandate id: {}", mandateExternalId);

            return customerWithBankAccount;
        } catch (Exception exc) {
            logException(exc, "bank account", mandateExternalId.toString());
            throw new CreateCustomerBankAccountFailedException(mandateExternalId, payer.getExternalId());
        }
    }

    private GoCardlessMandate createMandate(Mandate mandate, GoCardlessCustomer goCardlessCustomer) {
        try {

            LOGGER.info("Attempting to call GoCardless to create a mandate, pay mandate id: {}", mandate.getExternalId());
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(mandate.getGatewayAccount().getAccessToken());

            GoCardlessMandate goCardlessMandate = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);
            LOGGER.info("Created mandate in GoCardless, pay mandate id: {}, GoCardless mandate id: {}",
                    mandate.getExternalId(),
                    goCardlessMandate.getGoCardlessMandateId());

            return goCardlessMandate;
        } catch (Exception exc) {
            logException(exc, "mandate", mandate.getExternalId().toString());
            throw new CreateMandateFailedException(mandate.getExternalId().toString());
        }
    }

    private GoCardlessPayment createPayment(Transaction transaction, GoCardlessMandate goCardlessMandate) {
        try {
            LOGGER.info("Attempting to call GoCardless to create a payment, mandate id: {}, transaction id: {}",
                    transaction.getMandate().getExternalId(),
                    transaction.getExternalId());
            GoCardlessClientFacade goCardlessClientFacade = goCardlessClientFactory.getClientFor(transaction.getMandate().getGatewayAccount().getAccessToken());

            GoCardlessPayment goCardlessPayment = goCardlessClientFacade.createPayment(transaction, goCardlessMandate);

            LOGGER.info("Created payment in GoCardless, mandate id: {}, transaction id: {}, GoCardless payment id: {}",
                    transaction.getMandate().getExternalId(),
                    transaction.getExternalId(),
                    goCardlessPayment.getPaymentId());

            return goCardlessPayment;
        } catch (Exception exc) {
            logException(exc, "payment", transaction.getExternalId());
            throw new CreatePaymentFailedException(transaction.getMandate().getExternalId().toString(), transaction.getExternalId());
        }
    }

    private GoCardlessMandate findGoCardlessMandateForMandate(Mandate mandate) {
        return goCardlessMandateDao
                .findByMandateId(mandate.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find GoCardless mandate for mandate with id: {}", mandate.getExternalId());
                    return new GoCardlessMandateNotFoundException("mandate id", mandate.getExternalId().toString());
                });
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

    private void logException(Exception exc, String resource, String id) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in GoCardless, id : {}, error: {}, cause: {}", resource, id, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in GoCardless, id: {}, error: {}", resource, id, exc.getMessage());
        }
    }

}
