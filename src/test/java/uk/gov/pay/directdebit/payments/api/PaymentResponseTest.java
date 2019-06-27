package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;


public class PaymentResponseTest {

    @Test
    public void shouldNotStringifyPIIFields() {
        String transactionId = "id";
        Long amount = 10L;
        String description = "desc";
        String reference = "ref";
        var state = new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_STARTED, "test_details");
        var paymentResponse = aPaymentResponse()
                .withAmount(amount)
                .withCreatedDate(ZonedDateTime.now())
                .withDescription(description)
                .withReference(reference)
                .withTransactionExternalId(transactionId)
                .withState(state)
                .withDataLinks(new ArrayList<>())
                .build();
        assertFalse(paymentResponse.toString().contains(description));
    }
}
