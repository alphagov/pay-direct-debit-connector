package uk.gov.pay.directdebit.payments.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.exception.InvalidDateException;
import uk.gov.pay.directdebit.payments.exception.NegativeSearchParamException;
import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.aPaymentViewSearchParams;

public class PaymentViewValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PaymentViewValidator validator = new PaymentViewValidator();

    @Test
    public void shouldReturnNoErrors_withMinimumParams() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account").build(); 
        validator.validateParams(searchParams);
    }
    
    @Test
    public void shouldReturnNoErrors_withValidPagination() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .build();
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldReturnAnError_whenPageIsZero() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(0)
                .withDisplaySize(300)
                .build();
        thrown.expect(NegativeSearchParamException.class);
        thrown.expectMessage("Query param 'page' should be a non zero positive integer");
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldResetDisplaySizeTo500() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(600)
                .build();
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500));
    }

    @Test
    public void shouldSetPaginationWithDefaultValues() {
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account").build(); 
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500));
        assertThat(searchParams.getPaginationParams().getPageNumber(), is(0));
    }
    
    @Test
    public void shouldCorrectlyValidate_toAndFromDate() {
        String fromDate = "2018-05-03T15:00Z";
        String toDate = "2018-05-04T15:00Z";
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(42)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .build();
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getSearchDateParams().getFromDate().toString(), is(fromDate));
        assertThat(searchParams.getSearchDateParams().getToDate().toString(), is(toDate));
    }
    
    @Test 
    public void shouldLeaveToDateNull_whenMissing() {
        String fromDate = "2018-05-03T15:00Z";
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .withFromDateString(fromDate)
                .build();
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getSearchDateParams().getFromDate().toString(), is(fromDate));
        assertThat(searchParams.getSearchDateParams().getToDate(), is(nullValue()));
    }
    
    @Test
    public void shouldLeaveFromDateNull_whenMissing() {
        String toDate = "2018-05-04T15:00Z";
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .withToDateString(toDate)
                .build();
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getSearchDateParams().getFromDate(), is(nullValue()));
        assertThat(searchParams.getSearchDateParams().getToDate().toString(), is(toDate));
    }
    
    @Test
    public void shouldThrowInvalidDateException_whenFromDateIsAfterToDate() {
        String fromDate = "2018-05-05T15:00Z";
        String toDate = "2018-05-04T15:00Z";
        thrown.expect(InvalidDateException.class);
        thrown.expectMessage("from_date (2018-05-05T15:00Z) must be earlier then to_date (2018-05-04T15:00Z)");
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .build();
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenFromDateIsMalFormed() {
        String fromDate = "2018-03-05T15:00Z";
        String toDate = "2018-05-35T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input toDate (2018-05-35T15:00Z) is wrong format");
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(3)
                .withDisplaySize(256)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .build();
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenInvalidCharacterSingleQuote() {
        String fromDate = "2018-05-03T15:00Z'";
        String toDate = "2018-05-04T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input fromDate (2018-05-03T15:00Z') is wrong format");
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .build();
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenInvalidCharacterPercentage() {
        String fromDate = "%2018-05-03T15:00Z";
        String toDate = "2018-05-04T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input fromDate (%2018-05-03T15:00Z) is wrong format");
        PaymentViewSearchParams searchParams = aPaymentViewSearchParams("a-gateway-account")
                .withPage(2)
                .withDisplaySize(3)
                .withFromDateString(fromDate)
                .withToDateString(toDate)
                .build();
        validator.validateParams(searchParams);
    }
}
