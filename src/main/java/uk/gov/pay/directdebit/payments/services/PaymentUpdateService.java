package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import javax.inject.Inject;

import static java.lang.String.format;

public class PaymentUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentUpdateService.class);

    private final PaymentDao paymentDao;

    @Inject
    PaymentUpdateService(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    public Payment updateState(Payment payment, DirectDebitStateWithDetails<PaymentState> stateAndDetails) {
        String details = stateAndDetails.getDetails().orElse(null);
        String description = stateAndDetails.getDetailsDescription().orElse(null);
        
        paymentDao.updateStateAndDetails(payment.getId(),
                stateAndDetails.getState(),
                details,
                description);

        LOGGER.info(format("Updated status of payment %s to %s", payment.getExternalId(), stateAndDetails.getState()));
        
        return Payment.PaymentBuilder.fromPayment(payment)
                .withState(stateAndDetails.getState())
                .withStateDetails(details)
                .withStateDetailsDescription(description)
                .build();
    }

}
