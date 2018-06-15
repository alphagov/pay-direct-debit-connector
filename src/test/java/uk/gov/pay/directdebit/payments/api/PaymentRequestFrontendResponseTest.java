package uk.gov.pay.directdebit.payments.api;

import java.time.ZonedDateTime;
import org.junit.Test;
import uk.gov.pay.directdebit.mandate.api.ExternalMandateState;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.junit.Assert.assertFalse;

public class PaymentRequestFrontendResponseTest {
    
    @Test
    public void shouldNotStringifyPIIFields() {
        String paymentRequestId = "id";
        String returnUrl = "http://bla.bla";
        String reference = "ref";
        String createdDate = ZonedDateTime.now().toString();
        Payer payer = PayerFixture.aPayerFixture().toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
        PaymentRequestFrontendResponse paymentRequestResponse = new PaymentRequestFrontendResponse(
                paymentRequestId,
                3L,
                "aaaa",
                ExternalMandateState.EXTERNAL_INACTIVE,
                returnUrl,
                reference,
                createdDate,
                payer,
                transaction
        );
        assertFalse(paymentRequestResponse.toString().contains(payer.getEmail()));
        assertFalse(paymentRequestResponse.toString().contains(payer.getSortCode()));
        assertFalse(paymentRequestResponse.toString().contains(payer.getAccountNumber()));
    }
}
