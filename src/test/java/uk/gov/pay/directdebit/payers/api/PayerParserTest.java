package uk.gov.pay.directdebit.payers.api;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PayerParserTest {
    private static final String EMAIL = "aaa@bbb.com";
    private static final String SORT_CODE = "654321";
    private static final String ACCOUNT_NUMBER = "12345678";
    private static final String NAME = "name";

    private PayerParser payerParser = new PayerParser();

    @Test
    public void shouldCreateAPayerWhenAllFieldsAreThere() {
        Map<String, String> createTransactionWithAllFields = new HashMap<String, String>() {{
            put("account_holder_name", NAME);
            put("account_number", ACCOUNT_NUMBER);
            put("sort_code", SORT_CODE);
            put("email", EMAIL);
            put("requires_authorisation", "true");
        }};
        Payer payer = payerParser.parse(createTransactionWithAllFields, 10L);
        assertThat(payer.getExternalId(), is(notNullValue()));
        assertThat(payer.getMandateId(), is(10L));
        assertThat(payer.getName(), is(NAME));
        assertThat(payer.getEmail(), is(EMAIL));
        assertThat(payer.getAccountNumberLastTwoDigits(), is("78"));
        assertThat(payer.getAccountRequiresAuthorisation(), is(true));
        assertThat(BCrypt.checkpw(ACCOUNT_NUMBER, payer.getAccountNumber()), is(true));
        assertThat(BCrypt.checkpw(SORT_CODE, payer.getSortCode()), is(true));
        assertThat(payer.getCreatedDate(), is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }
}
