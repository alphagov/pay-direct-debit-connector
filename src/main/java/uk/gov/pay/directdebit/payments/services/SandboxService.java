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
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payers.model.BankAccountDetailsParser;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

public class SandboxService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(SandboxService.class);
    private static final int DAYS_TO_COLLECTION = 4;
    private final PayerService payerService;
    private final PaymentConfirmService paymentConfirmService;
    private final TransactionService transactionService;
    private final BankAccountDetailsValidator bankAccountDetailsValidator = new BankAccountDetailsValidator();
    
    @Inject
    public SandboxService(PayerService payerService,
            PaymentConfirmService paymentConfirmService,
            TransactionService transactionService) {
        this.payerService = payerService;
        this.paymentConfirmService = paymentConfirmService;
        this.transactionService = transactionService;
    }

    @Override
    public Payer createPayer(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> createPayerRequest) {
        LOGGER.info("Creating payer for SANDBOX, payment with id: {}", paymentRequestExternalId);
        return payerService.createOrUpdatePayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
    }

    @Override
    public void confirm(String paymentRequestExternalId, GatewayAccount gatewayAccount, Map<String, String> confirmDetailsRequest) {
        LOGGER.info("Confirming payment for SANDBOX, payment with id: {}", paymentRequestExternalId);
        ConfirmationDetails confirmationDetails = paymentConfirmService.confirm(gatewayAccount.getExternalId(), paymentRequestExternalId, confirmDetailsRequest);
        Payer payer = payerService.getPayerFor(confirmationDetails.getTransaction());
        transactionService.paymentSubmittedToProviderFor(confirmationDetails.getTransaction(), payer, confirmationDetails.getMandate(), LocalDate.now().plusDays(DAYS_TO_COLLECTION));
    }

    @Override
    public BankAccountValidationResponse validate(String paymentRequestExternalId, Map<String, String> bankAccountDetailsPayload) {
        LOGGER.info("Validating bank account details for SANDBOX, payment with id: {}", paymentRequestExternalId);
        try {
             bankAccountDetailsValidator.validate(bankAccountDetailsPayload);
        } catch (ValidationException exception) {
            LOGGER.warn("Bank details are invalid, payment with id: {}", paymentRequestExternalId);
            return new BankAccountValidationResponse(false);
        }
        LOGGER.info("Bank details are valid, payment with id: {}", paymentRequestExternalId);
        return new BankAccountValidationResponse(true, "Sandbox Bank");
    }
}
