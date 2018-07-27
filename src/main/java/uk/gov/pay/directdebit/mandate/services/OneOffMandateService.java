package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.api.CreatePaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class OneOffMandateService implements MandateCommandService {
    private final PaymentProviderFactory paymentProviderFactory;
    private final TransactionService transactionService;
    private final MandateStateUpdateService mandateStateUpdateService;
    private final MandateService mandateService;

    @Inject
    public OneOffMandateService(
            PaymentProviderFactory paymentProviderFactory,
            TransactionService transactionService,
            MandateStateUpdateService mandateStateUpdateService,
            MandateService mandateService) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.transactionService = transactionService;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.mandateService = mandateService;
    }

    public Transaction create(GatewayAccount gatewayAccount, CreatePaymentRequest createPaymentRequest) {
        Mandate mandate = mandateService.createMandate(createPaymentRequest, gatewayAccount.getExternalId());
        return transactionService.createTransaction(createPaymentRequest, mandate, gatewayAccount.getExternalId());
    }

    @Override
    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmMandateRequest) {
        mandateStateUpdateService.canUpdateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);

        Transaction transaction = transactionService
                .findTransactionForExternalId(confirmMandateRequest.getTransactionExternalId());

        OneOffConfirmationDetails oneOffConfirmationDetails = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .confirmOneOffMandate(mandate,
                        new BankAccountDetails(
                                confirmMandateRequest.getAccountNumber(),
                                confirmMandateRequest.getSortCode()),
                        transaction
                );

        mandateStateUpdateService.confirmedOneOffDirectDebitDetailsFor(oneOffConfirmationDetails.getMandate());
        transactionService.oneOffPaymentSubmittedToProviderFor(
                transaction,
                oneOffConfirmationDetails.getChargeDate());
    }
}
