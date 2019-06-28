package uk.gov.pay.directdebit.payments.api;

import uk.gov.pay.directdebit.payments.exception.InvalidDateException;
import uk.gov.pay.directdebit.payments.exception.NegativeSearchParamException;
import uk.gov.pay.directdebit.payments.exception.UnparsableDateException;
import uk.gov.pay.directdebit.payments.params.PaginationParams;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.params.SearchDateParams;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentViewValidator {

    private static final Long MAX_PAGE_NUMBER = 500L;
    private static final Long DEFAULT_PAGE_NUMBER = 1L;
    private static final String FROM_DATE_FIELD = "fromDate";
    private static final String TO_DATE_FIELD = "toDate";

    public PaymentViewSearchParams validateParams(PaymentViewSearchParams searchParams) {
        PaginationParams paginationParams = validatePagination(searchParams);
        SearchDateParams searchDateParams = validateSearchDate(searchParams);
        return new PaymentViewSearchParams(searchParams.getGatewayExternalId())
                .withPage(searchParams.getPage() == null ? DEFAULT_PAGE_NUMBER : searchParams.getPage())        
                .withDisplaySize(searchParams.getDisplaySize() == null ? MAX_PAGE_NUMBER : searchParams.getDisplaySize())
                .withFromDateString(searchParams.getFromDateString())
                .withToDateString(searchParams.getToDateString())
                .withReference(searchParams.getReference())
                .withAmount(searchParams.getAmount())
                .withMandateId(searchParams.getMandateId())
                .withPaginationParams(paginationParams)
                .withSearchDateParams(searchDateParams)
                .withState(searchParams.getState());
    }

    private PaginationParams validatePagination(PaymentViewSearchParams searchParams) {
        Long pageNumber = DEFAULT_PAGE_NUMBER - 1;
        Long displaySize = MAX_PAGE_NUMBER;
        PaginationParams paginationParams = searchParams.getPaginationParams();
        if (paginationParams.getDisplaySize() != null) {
            if (paginationParams.getDisplaySize() < 1) {
                throw new NegativeSearchParamException("display_size");    
            }
            displaySize = paginationParams.getDisplaySize() > MAX_PAGE_NUMBER ? MAX_PAGE_NUMBER : paginationParams.getDisplaySize();
        }
        if (paginationParams.getPageNumber() != null) {
            if (paginationParams.getPageNumber() < 1) {
                throw new NegativeSearchParamException("page");
            }
            pageNumber = (paginationParams.getPageNumber() - 1) * displaySize;
        }
        
        return new PaginationParams(pageNumber, displaySize);
    }

    private SearchDateParams validateSearchDate(PaymentViewSearchParams searchParams) {
        ZonedDateTime from = null;
        ZonedDateTime to = null;
        if (isNotBlank(searchParams.getFromDateString())) {
            from = parseDateTime(FROM_DATE_FIELD, searchParams.getFromDateString());
        }
        if (isNotBlank(searchParams.getToDateString())) {
            to = parseDateTime(TO_DATE_FIELD, searchParams.getToDateString());
        }
        validateFromDateIsBeforeToDate(from, to);
        return new SearchDateParams(from, to);
    }

    private ZonedDateTime parseDateTime(String fieldName, String dateToParse) {
        ZonedDateTime dateTime;
        try {
            dateTime = ZonedDateTime.parse(dateToParse);
        } catch (DateTimeParseException e) {
            throw new UnparsableDateException(fieldName, dateToParse);
        }
        return dateTime;
    }

    private void validateFromDateIsBeforeToDate(ZonedDateTime fromDate, ZonedDateTime toDate) {
        if (fromDate != null
                && toDate != null) {
            if (toDate
                    .isBefore(fromDate)) {
                throw new InvalidDateException(fromDate.toString(), toDate.toString());
            }
        }
    }
}

