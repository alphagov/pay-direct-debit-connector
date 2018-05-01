package uk.gov.pay.directdebit.payments.api;


import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertFalse;

public class PaymentViewResponseTest {

    @Test
    public void shouldNotStringifyPIIFields() {
        String gatewayExternalId = "1";
        String paymentRequestId = "id";
        Long amount = 10L;
        String returnUrl = "http://bla.bla";
        String description = "desc";
        String reference = "ref";
        String createdDate = ZonedDateTime.now().toString();
        String name = "name";
        String email = "email@email";
        PaymentViewResponse paymentViewResponse = new PaymentViewResponse(
                gatewayExternalId,
                paymentRequestId,
                amount,
                returnUrl,
                description,
                reference,
                createdDate,
                name,
                email,
                ExternalPaymentState.EXTERNAL_STARTED
                );
        assertFalse(paymentViewResponse.toString().contains(description));
    }
}
