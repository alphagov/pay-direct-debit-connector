package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
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
    private final MandateService mandateService;
    private final GoCardlessClientFacade goCardlessClientFacade;
    private final GoCardlessMandateDao goCardlessMandateDao;
    private final GoCardlessPaymentDao goCardlessPaymentDao;
    private final GoCardlessEventDao goCardlessEventDao;
    private final BankAccountDetailsParser bankAccountDetailsParser;

        @Inject
    public GoCardlessService(PayerService payerService,

            TransactionService transactionService, 
            MandateService mandateService,
            GoCardlessClientFacade goCardlessClientFacade,
            GoCardlessPaymentDao goCardlessPaymentDao,
            GoCardlessMandateDao goCardlessMandateDao,
            GoCardlessEventDao goCardlessEventDao,
            BankAccountDetailsParser bankAccountDetailsParser) {
        this.payerService = payerService;
        this.transactionService = transactionService;
        this.mandateService = mandateService;
        this.goCardlessClientFacade = goCardlessClientFacade;
        this.goCardlessMandateDao = goCardlessMandateDao;
        this.goCardlessPaymentDao = goCardlessPaymentDao;
        this.goCardlessEventDao = goCardlessEventDao;
        this.bankAccountDetailsParser = bankAccountDetailsParser;
    }

    @Override
    public Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        return payerService.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
    }

    @Override
    public Transaction collect(GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest) {
        String mandateExternalId = collectPaymentRequest.get("agreement_id");
        LOGGER.info("Collecting payment for GOCARDLESS, mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateService.findByExternalId(mandateExternalId);
        Transaction transaction = transactionService.createTransaction(
                collectPaymentRequest,
                mandate,
                gatewayAccount.getExternalId());
        GoCardlessMandate goCardlessMandate = findGoCardlessMandateForMandate(mandate);
        GoCardlessPayment payment = createPayment(transaction, goCardlessMandate);
        transactionService.onDemandPaymentSubmittedToProviderFor(transaction, payment.getChargeDate());
        LOGGER.info("Submitted payment collection for GOCARDLESS, for mandate with id: {}", mandateExternalId);
        return transaction;
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

    public GoCardlessMandate findGoCardlessMandateForEvent(GoCardlessEvent event) {
        return goCardlessMandateDao
                .findByEventResourceId(event.getResourceId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for event: {}", event.getJson());
                    return new GoCardlessMandateNotFoundException("resource id", event.getResourceId());
                });
    }

    private GoCardlessMandate findGoCardlessMandateForMandate(Mandate mandate) {
        return goCardlessMandateDao
                .findByMandateId(mandate.getId())
                .orElseThrow(() -> {
                    LOGGER.error("Couldn't find gocardless mandate for mandate with id: {}", mandate.getExternalId());
                    return new GoCardlessMandateNotFoundException("mandate id", mandate.getExternalId());
                });
    }

    private void logException(Exception exc, String resource, String id) {
        if (exc.getCause() != null) {
            LOGGER.error("Failed to create a {} in gocardless, id : {}, error: {}, cause: {}", resource, id, exc.getMessage(), exc.getCause().getMessage());
        } else {
            LOGGER.error("Failed to create a {} in gocardless, id: {}, error: {}", resource, id, exc.getMessage());
        }
    }
}
