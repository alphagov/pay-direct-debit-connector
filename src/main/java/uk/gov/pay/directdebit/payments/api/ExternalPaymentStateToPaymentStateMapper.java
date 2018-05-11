package uk.gov.pay.directdebit.payments.api;

import uk.gov.pay.directdebit.payments.model.PaymentState;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class ExternalPaymentStateToPaymentStateMapper {
    
    public static List<PaymentState> getPaymentState(ExternalPaymentState externalPaymentState) {
        return asList(PaymentState.values())
                            .stream()
                            .filter(paymentState -> paymentState.toExternal().equals(externalPaymentState))
                            .collect(Collectors.toList());
    }
}
