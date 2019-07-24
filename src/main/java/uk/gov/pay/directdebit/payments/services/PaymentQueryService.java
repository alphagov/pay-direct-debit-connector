package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.inject.Inject;
import java.util.Optional;

import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

public class PaymentQueryService {

    private final PaymentDao paymentDao;

    @Inject
    public PaymentQueryService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }
    
    public Optional<Payment> findByGoCardlessPaymentIdAndOrganisationId(
            GoCardlessPaymentId goCardlessPaymentId,
            GoCardlessOrganisationId goCardlessOrganisationId) {
        return paymentDao.findPaymentByProviderIdAndOrganisationId(GOCARDLESS, goCardlessPaymentId, goCardlessOrganisationId);
    }
    
    public Optional<Payment> findBySandboxPaymentId(SandboxPaymentId sandboxPaymentId) {
        return paymentDao.findPaymentByProviderId(SANDBOX, sandboxPaymentId);
    }

}
