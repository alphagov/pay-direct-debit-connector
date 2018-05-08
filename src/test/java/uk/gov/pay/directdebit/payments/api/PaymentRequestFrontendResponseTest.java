package uk.gov.pay.directdebit.payments.api;

import static org.junit.Assert.assertFalse;

import java.time.ZonedDateTime;
import org.junit.Test;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;

public class PaymentRequestFrontendResponseTest {
    
    @Test
    public void shouldNotStringifyPIIFields() {
        String paymentRequestId = "id";
        Long amount = 10L;
        String returnUrl = "http://bla.bla";
        String description = "desc";
        String reference = "ref";
        String createdDate = ZonedDateTime.now().toString();
        Payer payer = PayerFixture.aPayerFixture().toEntity();
        PaymentRequestFrontendResponse paymentRequestResponse = new PaymentRequestFrontendResponse(
                paymentRequestId,
                3L,
                "aaaa",
                ExternalPaymentState.EXTERNAL_STARTED,
                amount,
                returnUrl,
                description,
                reference,
                createdDate,
                payer
        );
        assertFalse(paymentRequestResponse.toString().contains(payer.getEmail()));
        assertFalse(paymentRequestResponse.toString().contains(payer.getSortCode()));
        assertFalse(paymentRequestResponse.toString().contains(payer.getAccountNumber()));
        assertFalse(paymentRequestResponse.toString().contains(payer.getAccountNumberLastTwoDigits()));
    }
}
