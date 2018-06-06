package uk.gov.pay.directdebit.payments.services;

import java.time.LocalDate;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.common.exception.validation.ValidationException;
import uk.gov.pay.directdebit.common.validation.BankAccountDetailsValidator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.services.MandateConfirmService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class SandboxService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(SandboxService.class);
    private static final int DAYS_TO_COLLECTION = 4;
    private final PayerService payerService;
    private final MandateConfirmService mandateConfirmService;
    private final TransactionService transactionService;
    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();
    
    @Inject
    public SandboxService(PayerService payerService,
            MandateConfirmService mandateConfirmService,
            TransactionService transactionService) {
        this.payerService = payerService;
        this.mandateConfirmService = mandateConfirmService;
        this.transactionService = transactionService;
    }

    @Override
    public Payer createPayer(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        LOGGER.info("Creating payer for SANDBOX, payment with id: {}", mandateExternalId);
        return payerService.createOrUpdatePayer(mandateExternalId, gatewayAccount, createPayerRequest);
    }

    @Override
    public void confirm(String mandateExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest) {
        LOGGER.info("Confirming payment for SANDBOX, mandate with id: {}", mandateExternalId);
        ConfirmationDetails confirmationDetails = mandateConfirmService
                .confirm(mandateExternalId, confirmDetailsRequest);
        Mandate mandate = confirmationDetails.getMandate();
        if (mandate.getType().equals(MandateType.ONE_OFF)) {
            Transaction transaction = confirmationDetails.getTransaction();
            transactionService.paymentSubmittedToProviderFor(transaction, LocalDate.now().plusDays(DAYS_TO_COLLECTION));
        }
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
