package uk.gov.pay.directdebit.payments.api;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CreatePaymentRequestTest {

    private static final String AMOUNT = "100";
    private static final String DESCRIPTION = "desc";
    private static final String RETURN_URL = "https://blabla.test";
    private static final String REFERENCE = "ref";

    @Test
    public void shouldCreateACreatePaymentRequest() {
        Map<String, String> createPaymentRequestMap = ImmutableMap.of(
                "amount", AMOUNT, 
                "description", DESCRIPTION,
                "return_url", RETURN_URL,
                "reference", REFERENCE);
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.of(createPaymentRequestMap);
        assertThat(createPaymentRequest.getAmount(), is(Long.parseLong(AMOUNT)));
        assertThat(createPaymentRequest.getDescription(), is(DESCRIPTION));
        assertThat(createPaymentRequest.getReference(), is(REFERENCE));
        assertThat(createPaymentRequest.getReturnUrl(), is(RETURN_URL));
    }
}
