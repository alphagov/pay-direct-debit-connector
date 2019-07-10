package uk.gov.pay.directdebit.payments.params;

public class PaginationParams {

    private Integer pageNumber;
    private Integer displaySize;

    public PaginationParams(Integer pageNumber, Integer displaySize) {
        this.pageNumber = pageNumber;
        this.displaySize = displaySize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }
}
