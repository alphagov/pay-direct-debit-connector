package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;


public class TransactionResponseTest {

    @Test
    public void shouldNotStringifyPIIFields() {
        String transactionId = "id";
        Long amount = 10L;
        String returnUrl = "http://bla.bla";
        String description = "desc";
        String reference = "ref";
        String createdDate = ZonedDateTime.now().toString();
        TransactionResponse transactionResponse = new TransactionResponse(
                transactionId,
                ExternalPaymentState.EXTERNAL_STARTED,
                amount,
                returnUrl,
                description,
                reference,
                createdDate,
                new ArrayList<>()
        );
        assertFalse(transactionResponse.toString().contains(description));
    }
}
