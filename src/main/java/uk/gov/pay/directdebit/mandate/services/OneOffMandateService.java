package uk.gov.pay.directdebit.mandate.services;

import javax.inject.Inject;

import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.OneOffConfirmationDetails;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.api.CreatePaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.services.TokenService;

public class OneOffMandateService extends MandateService {
    private final PaymentProviderFactory paymentProviderFactory;
    private final TransactionService transactionService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public OneOffMandateService(
            DirectDebitConfig directDebitConfig,
            MandateDao mandateDao, GatewayAccountDao gatewayAccountDao,
            TokenService tokenService,
            TransactionService transactionService,
            MandateStateUpdateService mandateStateUpdateService,
            PaymentProviderFactory paymentProviderFactory,
            MandateService mandateService) {
        super(directDebitConfig, mandateDao, gatewayAccountDao, tokenService, transactionService, mandateStateUpdateService);
        this.paymentProviderFactory = paymentProviderFactory;
        this.transactionService = transactionService;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }
    
/*    public Transaction create(GatewayAccount gatewayAccount, CreatePaymentRequest createPaymentRequest) {
        Mandate mandate = mandateService.createMandate(createPaymentRequest, gatewayAccount.getExternalId());
        return transactionService.createTransaction(createPaymentRequest, mandate, gatewayAccount.getExternalId());
    }*/

    @Override
    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmMandateRequest) {
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

        mandateStateUpdateService.confirmedDirectDebitDetailsFor(oneOffConfirmationDetails.getMandate());
        transactionService.oneOffPaymentSubmittedToProviderFor(
                transaction,
                oneOffConfirmationDetails.getChargeDate());
    }
}
