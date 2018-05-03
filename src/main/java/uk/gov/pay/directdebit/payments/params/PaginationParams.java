package uk.gov.pay.directdebit.payments.params;

public class PaginationParams {

    private Long pageNumber;
    private Long displaySize;

    public PaginationParams(Long pageNumber, Long displaySize) {
        this.pageNumber = pageNumber;
        this.displaySize = displaySize;
    }

    public Long getPageNumber() {
        return pageNumber;
    }

    public Long getDisplaySize() {
        return displaySize;
    }
}
