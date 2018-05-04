package uk.gov.pay.directdebit.payments.dao;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PaymentViewSearchParamsTest {

    private PaymentViewSearchParams searchParams;

    @Test
    public void shouldGenerateQueryString() {
        searchParams = new PaymentViewSearchParams("account-id", 1l, 100l);
        assertThat(searchParams.generateQuery(), is(notNullValue()));
    }

    @Test
    public void shouldCreateQueryMap() {
        searchParams = new PaymentViewSearchParams("account-id", 1l, 100l);
        assertThat(searchParams.getQueryMap().containsValue("account-id"), is(true));
        assertThat(searchParams.getQueryMap().containsValue(100l), is(true));
        assertThat(searchParams.getQueryMap().containsKey("offset"), is(true));
    }
}
