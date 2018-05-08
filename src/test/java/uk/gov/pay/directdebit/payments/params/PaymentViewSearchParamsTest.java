package uk.gov.pay.directdebit.payments.params;


import org.junit.Test;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PaymentViewSearchParamsTest {

    private PaymentViewSearchParams searchParams;
    private ZonedDateTime fromDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
    private ZonedDateTime toDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    
    @Test
    public void shouldCreateQueryMap() {
        searchParams = new PaymentViewSearchParams("account-id", 
                2l, 600l, fromDate.toString(), toDate.toString());
        searchParams = new PaymentViewValidator().validateParams(searchParams);
        assertThat(searchParams.getQueryMap().get("gatewayAccountExternalId"), is("account-id"));
        assertThat(searchParams.getQueryMap().get("offset"), is(500L));
        assertThat(searchParams.getQueryMap().get("limit"), is(500L));
        assertThat(searchParams.getQueryMap().containsKey("fromDate"), is(true));
        assertThat(searchParams.getQueryMap().containsKey("toDate"), is(true));
    }
}
