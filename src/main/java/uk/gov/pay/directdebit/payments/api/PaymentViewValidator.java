package uk.gov.pay.directdebit.payments.api;

import uk.gov.pay.directdebit.payments.dao.PaymentViewSearchParams;
import uk.gov.pay.directdebit.payments.exception.NegativeSearchParamException;
import uk.gov.pay.directdebit.payments.params.PaginationParams;

public class PaymentViewValidator {

    private static final Long MAX_PAGE_NUMBER = 500l;
    private static final Long DEFAULT_PAGE_NUMBER = 1l;

    public void validateParams(PaymentViewSearchParams searchParams) {
        searchParams.setPaginationParams(validatePagination(searchParams.getPaginationParams()));
    }

    private PaginationParams validatePagination(PaginationParams paginationParams) {
        Long pageNumber = DEFAULT_PAGE_NUMBER;
        Long displaySize = MAX_PAGE_NUMBER;
        if (paginationParams.getPageNumber() != null && paginationParams.getPageNumber() < 1) {
            throw new NegativeSearchParamException("page");
        }
        if (paginationParams.getDisplaySize() != null && paginationParams.getDisplaySize() < 1) {
            throw new NegativeSearchParamException("display_size");
        }
        if (paginationParams.getDisplaySize() != null) {
            displaySize = paginationParams.getDisplaySize() > MAX_PAGE_NUMBER ? MAX_PAGE_NUMBER : paginationParams.getDisplaySize();
        }
        if (paginationParams.getPageNumber() != null) {
            pageNumber = (paginationParams.getPageNumber() - 1) * displaySize;
        }
        return new PaginationParams(pageNumber, displaySize);
    }
}
