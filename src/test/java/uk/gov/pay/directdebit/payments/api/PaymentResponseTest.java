package uk.gov.pay.directdebit.payments.api;

import org.junit.Test;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

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
        MandateExternalId mandateExternalId = MandateExternalId.valueOf("mandateid");
        var state = new ExternalPaymentStateWithDetails(ExternalPaymentState.EXTERNAL_STARTED, "test_details");
        var paymentResponse = aPaymentResponse()
                .withCreatedDate(ZonedDateTime.now())
                .withState(state)
                .withDescription(description)
                .withReference(reference)
                .withAmount(amount)
                .withTransactionExternalId(transactionId)
                .withMandateId(mandateExternalId)
                .build();
        assertFalse(paymentResponse.toString().contains(description));
    }
}
