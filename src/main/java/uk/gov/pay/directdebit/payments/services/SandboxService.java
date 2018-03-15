package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

import javax.inject.Inject;
import java.util.Map;

public class SandboxService implements DirectDebitPaymentProvider {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(SandboxService.class);

    private final PayerService payerService;
    private final PaymentConfirmService paymentConfirmService;
    private final TransactionService transactionService;

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
        return payerService.create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest);
    }

    @Override
    public void confirm(String paymentRequestExternalId, GatewayAccount gatewayAccount) {
        LOGGER.info("Confirming payment for SANDBOX, payment with id: {}", paymentRequestExternalId);
        ConfirmationDetails confirmationDetails = paymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId);
        transactionService.paymentCreatedFor(confirmationDetails.getTransaction());
    }
}
