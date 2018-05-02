package uk.gov.pay.directdebit.payments.dao;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public class PaymentViewSearchParams {

    private Long page;
    private Long displaySize;
    private Optional<Pair<Long, Long>> paginationParams;

    public PaymentViewSearchParams(Long page, Long displaySize) {
        this.page = page;
        this.displaySize = displaySize;
    }

    public Long getPage() {
        return page;
    }

    public Long getDisplaySize() {
        return displaySize;
    }

    public Optional<Pair<Long, Long>> getPaginationParams() {
        if (paginationParams == null) {
            if (page == null || displaySize == null) {
                paginationParams = Optional.empty();
            }
            paginationParams = Optional.of(Pair.of(page, displaySize));
        }
        return paginationParams;
    }
}
