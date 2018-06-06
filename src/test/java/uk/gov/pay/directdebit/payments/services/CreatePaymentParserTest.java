package uk.gov.pay.directdebit.payments.services;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Test;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CreatePaymentParserTest {
    private static final String AMOUNT = "123";
    private static final String DESCRIPTION = "description";
    private static final String REFERENCE = "a-reference";
    
    private CreatePaymentParser createPaymentParser = new CreatePaymentParser();

    @Test
    public void shouldCreateATransactionWhenPaymentRequestIsValid() {
        MandateFixture mandateFixture = MandateFixture.aMandateFixture();
        Map<String, String> createPaymentRequest = new HashMap<String, String>() {{
            put("amount", AMOUNT);
            put("description", DESCRIPTION);
            put("reference", REFERENCE);
        }};
        Transaction transaction = createPaymentParser.parse(createPaymentRequest, mandateFixture.toEntity());
        assertThat(transaction.getExternalId(), is(notNullValue()));
        assertThat(transaction.getReference(), is(REFERENCE));
        assertThat(transaction.getDescription(), is(DESCRIPTION));
        assertThat(transaction.getAmount(), is(Long.parseLong(AMOUNT)));
        assertThat(transaction.getState(), is(PaymentState.NEW));
        assertThat(transaction.getMandate(), is(mandateFixture.toEntity()));
        assertThat(transaction.getCreatedDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }
}
