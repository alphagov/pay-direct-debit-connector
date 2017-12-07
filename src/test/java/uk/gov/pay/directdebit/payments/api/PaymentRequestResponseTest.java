package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static junit.framework.Assert.assertFalse;


public class PaymentRequestResponseTest {

    @Test
    public void shouldNotStringifyPIIFields() {
        String paymentRequestId = "id";
        Long amount = 10L;
        String returnUrl = "http://bla.bla";
        String description = "desc";
        String reference = "ref";
        String createdDate = ZonedDateTime.now().toString();
     PaymentRequestResponse paymentRequestResponse = new PaymentRequestResponse(
             paymentRequestId,
             amount,
             returnUrl,
             description,
             reference,
             createdDate,
             new ArrayList<>()
     );
     assertFalse(paymentRequestResponse.toString().contains(description));
    }
}
