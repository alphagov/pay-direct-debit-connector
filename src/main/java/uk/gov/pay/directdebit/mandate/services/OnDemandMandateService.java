package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class OnDemandMandateService {
    private PaymentProviderFactory paymentProviderFactory;
    private MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public OnDemandMandateService(PaymentProviderFactory paymentProviderFactory, 
                                  MandateStateUpdateService mandateStateUpdateService) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }

    public void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmDetailsRequest) {

        if (mandateStateUpdateService.canUpdateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED)) {
            Mandate confirmedMandate = paymentProviderFactory
                    .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                    .confirmOnDemandMandate(
                            mandate,
                            new BankAccountDetails(
                                    confirmDetailsRequest.getAccountNumber(),
                                    confirmDetailsRequest.getSortCode())
                    );
            mandateStateUpdateService.confirmedOnDemandDirectDebitDetailsFor(confirmedMandate);
        } else {
            throw new InvalidStateTransitionException(DIRECT_DEBIT_DETAILS_CONFIRMED.toString(), mandate.getState().toString());
        }
    }
}
