package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdAndOrganisationId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentLookupKey;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.inject.Inject;

public class PaymentQueryService {

    private final PaymentDao paymentDao;

    @Inject
    public PaymentQueryService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    public Payment findByProviderPaymentId(PaymentProvider paymentProvider, PaymentLookupKey paymentLookupKey) {
        if (paymentLookupKey.getClass() == GoCardlessPaymentIdAndOrganisationId.class) {
            var goCardlessPaymentIdAndOrganisationId = (GoCardlessPaymentIdAndOrganisationId) paymentLookupKey;
            return paymentDao
                    .findPaymentByProviderIdAndOrganisationId(paymentProvider, goCardlessPaymentIdAndOrganisationId.getGoCardlessPaymentId(),
                            goCardlessPaymentIdAndOrganisationId.getGoCardlessOrganisationId())
                    .orElseThrow(() -> new PaymentNotFoundException(goCardlessPaymentIdAndOrganisationId));
        } else if (paymentLookupKey.getClass() == SandboxPaymentId.class) {
            var paymentProviderPaymentId = (PaymentProviderPaymentId) paymentLookupKey;
            return paymentDao
                    .findPaymentByProviderId(paymentProvider, paymentProviderPaymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentProvider, paymentProviderPaymentId));
        }
        throw new IllegalArgumentException("Unrecognised PaymentLookupKey of type " + paymentLookupKey.getClass());
    }

}
