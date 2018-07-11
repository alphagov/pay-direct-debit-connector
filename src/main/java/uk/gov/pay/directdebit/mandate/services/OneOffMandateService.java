package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationRequest;
import uk.gov.pay.directdebit.mandate.model.OneOffMandateConfirmationDetails;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import java.time.LocalDate;

public class OneOffMandateService implements MandateCommandService {
    private final PaymentProviderFactory paymentProviderFactory;
    private final TransactionService transactionService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public OneOffMandateService(
            PaymentProviderFactory paymentProviderFactory,
            TransactionService transactionService,
            MandateStateUpdateService mandateStateUpdateService) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.transactionService = transactionService;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }

    @Override
    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, MandateConfirmationRequest mandateConfirmationRequest) {
        Transaction transaction = transactionService
                .findTransactionForExternalId(mandateConfirmationRequest.getTransactionExternalId());

        paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .confirmMandate(OneOffMandateConfirmationDetails.from(
                        mandate,
                        transaction,
                        mandateConfirmationRequest
                ));

        mandateStateUpdateService.confirmedDirectDebitDetailsFor(mandate);
        transactionService.oneOffPaymentSubmittedToProviderFor(transaction, LocalDate.now().plusDays(4));

    }
}
