package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.PaymentNotFoundException;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

public class PaymentQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentQueryService.class);

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

    public Payment findPaymentForExternalId(String externalId) {
        Payment payment = paymentDao.findByExternalId(externalId)
                .orElseThrow(() -> new PaymentNotFoundException(externalId));
        LOGGER.info("Found charge for payment with id: {}", externalId);
        return payment;
    }

    public PaymentResponse getPaymentWithExternalId(String paymentExternalId) {
        Payment payment = findPaymentForExternalId(paymentExternalId);
        return PaymentResponse.from(payment);
    }


    public List<Payment> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return paymentDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }

}
