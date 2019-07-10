package uk.gov.pay.directdebit.payments.params;


import org.junit.Test;
import uk.gov.pay.directdebit.payments.api.PaymentViewValidator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.aPaymentViewSearchParams;

public class PaymentViewSearchParamsTest {

    private PaymentViewSearchParams searchParams;
    private ZonedDateTime fromDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2);
    private ZonedDateTime toDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    
    @Test
    public void shouldCreateQueryMap() {
        searchParams = aPaymentViewSearchParams("account-id")
                .withPage(2L)
                .withDisplaySize(600L)
                .withFromDateString(fromDate.toString())
                .withToDateString(toDate.toString())
                .build();        
        searchParams = new PaymentViewValidator().validateParams(searchParams);
        assertThat(searchParams.getQueryMap().get("gatewayAccountExternalId"), is("account-id"));
        assertThat(searchParams.getQueryMap().get("offset"), is(500L));
        assertThat(searchParams.getQueryMap().get("limit"), is(500L));
        assertThat(searchParams.getQueryMap().containsKey("fromDate"), is(true));
        assertThat(searchParams.getQueryMap().containsKey("toDate"), is(true));
    }
}
