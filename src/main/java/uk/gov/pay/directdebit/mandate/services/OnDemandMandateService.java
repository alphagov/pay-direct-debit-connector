package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationRequest;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationDetails;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;

public class OnDemandMandateService implements MandateCommandService {
    private PaymentProviderFactory paymentProviderFactory;
    private MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public OnDemandMandateService(
            PaymentProviderFactory paymentProviderFactory,
            MandateStateUpdateService mandateStateUpdateService) {
        this.paymentProviderFactory = paymentProviderFactory;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }

    @Override
    public void confirm(
            GatewayAccount gatewayAccount,
            Mandate mandate,
            MandateConfirmationRequest confirmDetailsRequest) {
        
        mandateStateUpdateService.canUpdateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);
        paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .confirmMandate(MandateConfirmationDetails.from(
                        mandate,
                        confirmDetailsRequest
                ));
        mandateStateUpdateService.confirmedDirectDebitDetailsFor(mandate);
    }
}
