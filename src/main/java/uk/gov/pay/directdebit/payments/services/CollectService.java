package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.MandateStateInvalidException;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.exception.MandateNotSubmittedToProviderException;
import uk.gov.pay.directdebit.payments.model.Payment;

import javax.inject.Inject;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED_TO_PROVIDER;

public class CollectService {

    private static final Set<MandateState> MANDATE_STATES_ALLOWING_PAYMENT_COLLECTION = Collections.unmodifiableSet(EnumSet.of(
            SUBMITTED_TO_PROVIDER,
            SUBMITTED_TO_BANK,
            ACTIVE
    ));

    private final MandateQueryService mandateQueryService;
    private final PaymentService paymentService;

    @Inject
    public CollectService(MandateQueryService mandateQueryService, PaymentService paymentService) {
        this.mandateQueryService = mandateQueryService;
        this.paymentService = paymentService;
    }

    public Payment collect(GatewayAccount gatewayAccount, CollectPaymentRequest collectPaymentRequest) {
        var mandate = mandateQueryService.findByExternalIdAndGatewayAccountExternalId(collectPaymentRequest.getMandateExternalId(),
                gatewayAccount.getExternalId());

        if (!MANDATE_STATES_ALLOWING_PAYMENT_COLLECTION.contains(mandate.getState())) {
            throw new MandateStateInvalidException(format("Mandate state invalid for Mandate with id: %s", mandate.getExternalId()));
        }

        var paymentProviderMandateId = mandate.getPaymentProviderMandateId()
                .orElseThrow(() -> new MandateNotSubmittedToProviderException(mandate.getExternalId()));

        var payment = paymentService.createPayment(collectPaymentRequest.getAmount(), collectPaymentRequest.getDescription(),
                collectPaymentRequest.getReference(), mandate);

        return paymentService.submitPaymentToProvider(payment, paymentProviderMandateId);
    }

}
