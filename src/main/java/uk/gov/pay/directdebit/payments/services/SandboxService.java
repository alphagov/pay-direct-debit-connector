package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.exception.validation.ValidationException;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.OneOffMandateConfirmationDetails;
import uk.gov.pay.directdebit.mandate.services.MandateService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvideCommandService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

public class SandboxService implements DirectDebitPaymentProvider, DirectDebitPaymentProvideCommandService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(SandboxService.class);
    private static final int DAYS_TO_COLLECTION = 4;
    private final PayerService payerService;
    private final TransactionService transactionService;
    private final MandateService mandateService;
    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();

    @Inject
    public SandboxService(PayerService payerService,
                          MandateService mandateService,
                          TransactionService transactionService) {
        this.payerService = payerService;
        this.transactionService = transactionService;
        this.mandateService = mandateService;
    }

    @Override
    public Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        LOGGER.info("Creating payer for SANDBOX, payment with id: {}", mandateExternalId);
        return payerService.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
    }

    @Override
    public void confirmMandate(OneOffMandateConfirmationDetails oneOffMandateConfirmationDetails) {
        Mandate mandate = oneOffMandateConfirmationDetails.getMandate();
        LOGGER.info("Confirming payment for SANDBOX, mandate with id: {}", mandate.getExternalId());
        Transaction transaction = oneOffMandateConfirmationDetails.getTransaction();
        transactionService.oneOffPaymentSubmittedToProviderFor(transaction, LocalDate.now().plusDays(DAYS_TO_COLLECTION));
    }

    @Override
    public void confirmMandate(MandateConfirmationDetails mandateConfirmationDetails) {
        //no op
    }

    @Override
    public Transaction collect(GatewayAccount gatewayAccount, Map<String, String> collectPaymentRequest) {
        String mandateExternalId = collectPaymentRequest.get("agreement_id");
        LOGGER.info("Collecting payment for SANDBOX, mandate with id: {}", mandateExternalId);
        Mandate mandate = mandateService.findByExternalId(mandateExternalId);
        Transaction transaction = transactionService.createTransaction(
                collectPaymentRequest,
                mandate,
                gatewayAccount.getExternalId());
        transactionService.onDemandPaymentSubmittedToProviderFor(transaction, LocalDate.now().plusDays(DAYS_TO_COLLECTION));
        LOGGER.info("Submitted payment collection for SANDBOX, for mandate with id: {}", mandateExternalId);
        return transaction;
    }

    @Override
    public BankAccountValidationResponse validate(String mandateExternalId, Map<String, String> bankAccountDetailsPayload) {
        LOGGER.info("Validating bank account details for SANDBOX, mandate with id: {}", mandateExternalId);
        try {
            bankAccountDetailsValidator.validate(bankAccountDetailsPayload);
        } catch (ValidationException exception) {
            LOGGER.warn("Bank details are invalid, mandate with id: {}", mandateExternalId);
            return new BankAccountValidationResponse(false);
        }
        LOGGER.info("Bank details are valid, mandate with id: {}", mandateExternalId);
        return new BankAccountValidationResponse(true, "Sandbox Bank");
    }
}
