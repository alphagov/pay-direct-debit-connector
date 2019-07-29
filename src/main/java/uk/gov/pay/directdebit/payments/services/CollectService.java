package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.MandateStateInvalidException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.model.Payment;

import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_PROVIDER;

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

        if (!List.of(SUBMITTED_TO_PROVIDER, ACTIVE).contains(mandate.getState())) {
            throw new MandateStateInvalidException(format("Mandate state invalid for Mandate with id: %s", mandate.getExternalId()));
        }

        Payment payment = paymentService.createPayment(collectPaymentRequest.getAmount(), collectPaymentRequest.getDescription(),
                collectPaymentRequest.getReference(), mandate);

        return paymentService.submitPaymentToProvider(payment);
    }

}
