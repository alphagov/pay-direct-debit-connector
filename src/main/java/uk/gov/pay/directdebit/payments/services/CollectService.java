package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.MandateStateInvalidException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.model.Payment;

import javax.inject.Inject;

public class CollectService {

    private final MandateQueryService mandateQueryService;
    private final PaymentService paymentService;

    @Inject
    public CollectService(MandateQueryService mandateQueryService, PaymentService paymentService) {
        this.mandateQueryService = mandateQueryService;
        this.paymentService = paymentService;
    }

    public Payment collect(GatewayAccount gatewayAccount, CollectPaymentRequest collectPaymentRequest) {
        Mandate mandate = mandateQueryService.findByExternalIdAndGatewayAccountExternalId(collectPaymentRequest.getMandateExternalId(),
                gatewayAccount.getExternalId());

        if (!(mandate.getState().equals(MandateState.SUBMITTED_TO_PROVIDER) || mandate.getState().equals(MandateState.PENDING) ||
                mandate.getState().equals(MandateState.ACTIVE))) {
            throw new MandateStateInvalidException("Mandate state invalid for Mandate with id: " +
                    mandate.getExternalId());
        }

        Payment payment = paymentService.createPayment(collectPaymentRequest.getAmount(), collectPaymentRequest.getDescription(),
                collectPaymentRequest.getReference(), mandate);

        return paymentService.submitPaymentToProvider(payment);
    }

}
