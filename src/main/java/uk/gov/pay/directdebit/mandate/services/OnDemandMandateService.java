package uk.gov.pay.directdebit.mandate.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.exception.InvalidMandateTypeException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class OnDemandMandateService extends MandateService{
    private PaymentProviderFactory paymentProviderFactory;
    private MandateStateUpdateService mandateStateUpdateService;


    @Inject
    public OnDemandMandateService(
            DirectDebitConfig directDebitConfig,
            MandateDao mandateDao, GatewayAccountDao gatewayAccountDao,
            TokenService tokenService,
            TransactionService transactionService,
            MandateStateUpdateService mandateStateUpdateService,
            PaymentProviderFactory paymentProviderFactory) {
        super(directDebitConfig, mandateDao, gatewayAccountDao, tokenService, transactionService, mandateStateUpdateService);
        this.paymentProviderFactory = paymentProviderFactory;
        this.mandateStateUpdateService = mandateStateUpdateService;
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
        mandateStateUpdateService.confirmedDirectDebitDetailsFor(confirmedMandate);
    }

/*    public Transaction collect(GatewayAccount gatewayAccount, Mandate mandate,
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
    }*/

/*    public CreateMandateResponse create(GatewayAccount gatewayAccount, CreateMandateRequest createMandateRequest,
                                        UriInfo uriInfo) {
        if (MandateType.ONE_OFF.equals(createMandateRequest.getMandateType())) {
            throw new InvalidMandateTypeException(MandateType.ONE_OFF);
        }

        String gatewayAccountExternalId = gatewayAccount.getExternalId();
        Mandate mandate = mandateService.createMandate(createMandateRequest, gatewayAccountExternalId);

        return mandateService.populateCreateMandateResponse(mandate, gatewayAccountExternalId, uriInfo);
    }*/

}
