package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.exception.InvalidMandateTypeException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class OnDemandMandateService implements MandateCommandService {
    private PaymentProviderFactory paymentProviderFactory;
    private TransactionService transactionService;
    private MandateStateUpdateService mandateStateUpdateService;
    private MandateService mandateService;

    @Inject
    public OnDemandMandateService(
            PaymentProviderFactory paymentProviderFactory,
            MandateStateUpdateService mandateStateUpdateService,
            TransactionService transactionService,
            MandateService mandateService) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.mandateStateUpdateService = mandateStateUpdateService;
        this.transactionService = transactionService;
        this.mandateService = mandateService;
    }

    @Override
    public void confirm(
            GatewayAccount gatewayAccount,
            Mandate mandate,
            ConfirmMandateRequest confirmDetailsRequest) {

        mandateStateUpdateService.canUpdateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);
        Mandate confirmedMandate = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .confirmOnDemandMandate(
                        mandate,
                        new BankAccountDetails(
                                confirmDetailsRequest.getAccountNumber(),
                                confirmDetailsRequest.getSortCode())
                );
        mandateStateUpdateService.confirmedOnDemandDirectDebitDetailsFor(confirmedMandate);
    }

    public Transaction collect(GatewayAccount gatewayAccount, Mandate mandate,
            CollectPaymentRequest collectPaymentRequest) {
        if (MandateType.ONE_OFF.equals(mandate.getType())) {
            throw new InvalidMandateTypeException(mandate.getExternalId(), MandateType.ONE_OFF);
        }
        Transaction transaction = transactionService.createTransaction(
                collectPaymentRequest,
                mandate,
                gatewayAccount.getExternalId());
        
        LocalDate chargeDate = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .collect(mandate, transaction);
        transactionService.onDemandPaymentSubmittedToProviderFor(transaction, chargeDate);
        return transaction;
    }

    public CreateMandateResponse create(GatewayAccount gatewayAccount, CreateMandateRequest createMandateRequest,
            UriInfo uriInfo) {
        if (MandateType.ONE_OFF.equals(createMandateRequest.getMandateType())) {
            throw new InvalidMandateTypeException(MandateType.ONE_OFF);
        }
        return mandateService.createMandate(createMandateRequest, gatewayAccount.getExternalId(), uriInfo);
    }

}
