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
        PaymentResponse paymentResponse = new PaymentResponse(
                transactionId,
                ExternalPaymentState.EXTERNAL_STARTED,
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
