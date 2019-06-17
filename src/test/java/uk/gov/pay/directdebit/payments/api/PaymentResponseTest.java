package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;


public class PaymentResponseTest {

    @Test
    public void shouldNotStringifyPIIFields() {
        String transactionId = "id";
        Long amount = 10L;
        String returnUrl = "http://bla.bla";
        String description = "desc";
        String reference = "ref";
        var state = new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_STARTED, "test_details");
        PaymentResponse paymentResponse = new PaymentResponse(
                transactionId,
                state,
                amount,
                returnUrl,
                description,
                reference,
                ZonedDateTime.now(),
                new ArrayList<>()
        );
        assertFalse(paymentResponse.toString().contains(description));
    }
}
