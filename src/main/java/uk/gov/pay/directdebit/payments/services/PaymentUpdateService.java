package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.PaymentLookupKey;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.inject.Inject;

public class PaymentUpdateService {

    private final PaymentDao paymentDao;

    @Inject
    PaymentUpdateService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    public int updateStateByProviderId(PaymentProvider paymentProvider, PaymentLookupKey paymentLookupKey,
                                       DirectDebitStateWithDetails<PaymentState> stateAndDetails) {
        if (paymentLookupKey.getClass() == GoCardlessPaymentIdAndOrganisationId.class) {
            var goCardlessPaymentIdAndOrganisationId = (GoCardlessPaymentIdAndOrganisationId) paymentLookupKey;
            return paymentDao.updateStateByProviderIdAndOrganisationId(paymentProvider, goCardlessPaymentIdAndOrganisationId.getGoCardlessOrganisationId(),
                    goCardlessPaymentIdAndOrganisationId.getGoCardlessPaymentId(), stateAndDetails.getState(), stateAndDetails.getDetails().orElse(null),
                    stateAndDetails.getDetailsDescription().orElse(null));
        } else if (paymentLookupKey.getClass() == SandboxPaymentId.class) {
            var paymentProviderPaymentId = (PaymentProviderPaymentId) paymentLookupKey;
            return paymentDao.updateStateByProviderId(paymentProvider, paymentProviderPaymentId, stateAndDetails.getState(),
                    stateAndDetails.getDetails().orElse(null), stateAndDetails.getDetailsDescription().orElse(null));
        }
        throw new IllegalArgumentException("Unrecognised PaymentLooupKey of type " + paymentLookupKey.getClass());
    }

}
