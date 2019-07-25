package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.Optional;

public interface PaymentStateCalculator {
    Optional<DirectDebitStateWithDetails<PaymentState>> calculate(Payment payment);
}
