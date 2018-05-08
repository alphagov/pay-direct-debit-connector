package uk.gov.pay.directdebit.payments.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.exception.InvalidDateException;
import uk.gov.pay.directdebit.payments.exception.NegativeSearchParamException;
import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class PaymentViewValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private PaymentViewValidator validator = new PaymentViewValidator();

    @Test
    public void shouldReturnNoErrorsWithMinimumParams() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 
                3L, 256L, null, null);
        validator.validateParams(searchParams);
    }
    
    @Test
    public void shouldReturnNoErrors_withValidPagination() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                2L, 3L, null, null);
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldReturnAnError_whenPageIsZero() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 
                0L, 256L, null, null);
        thrown.expect(NegativeSearchParamException.class);
        thrown.expectMessage("Query param 'page' should be a non zero positive integer");
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldResetDisplaySizeTo500() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", 
                2L, 600L, null, null);
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500L));
    }

    @Test
    public void shouldSetPaginationWithDefaultValues() {
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account", null, null, null, null);
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getPaginationParams().getDisplaySize(), is(500L));
        assertThat(searchParams.getPaginationParams().getPageNumber(), is(1L));
    }
    
    @Test
    public void shouldCorrectlyValidate_toAndFromDate() {
        String fromDate = "2018-05-03T15:00Z";
        String toDate = "2018-05-04T15:00Z";
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L, 
                256L,
                fromDate,
                toDate);
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getSearchDateParams().getFromDate().toString(), is(fromDate));
        assertThat(searchParams.getSearchDateParams().getToDate().toString(), is(toDate));
    }
    
    @Test 
    public void shouldLeaveToDateNull_whenMissing() {
        String fromDate = "2018-05-03T15:00Z";
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                fromDate,
                null);
        searchParams = validator.validateParams(searchParams);
        assertThat(searchParams.getSearchDateParams().getFromDate().toString(), is(fromDate));
        assertThat(searchParams.getSearchDateParams().getToDate(), is(nullValue()));
    }
    
    @Test
    public void shouldLeaveFromDateNull_whenMissing() {
        String toDate = "2018-05-04T15:00Z";
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                null,
                toDate);
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
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                fromDate,
                toDate);
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenFromDateIsMalFormed() {
        String fromDate = "2018-03-05T15:00Z";
        String toDate = "2018-05-35T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input toDate (2018-05-35T15:00Z) is wrong format");
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                fromDate,
                toDate);
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenInvalidCharacterSingleQuote() {
        String fromDate = "2018-05-03T15:00Z'";
        String toDate = "2018-05-04T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input fromDate (2018-05-03T15:00Z') is wrong format");
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                fromDate,
                toDate);
        validator.validateParams(searchParams);
    }

    @Test
    public void shouldThrowUnparsableDateException_whenInvalidCharacterPercentage() {
        String fromDate = "%2018-05-03T15:00Z";
        String toDate = "2018-05-04T15:00Z";
        thrown.expect(UnparsableDateException.class);
        thrown.expectMessage("Input fromDate (%2018-05-03T15:00Z) is wrong format");
        PaymentViewSearchParams searchParams = new PaymentViewSearchParams("a-gateway-account",
                3L,
                256L,
                fromDate,
                toDate);
        validator.validateParams(searchParams);
    }
}
