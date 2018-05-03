package uk.gov.pay.directdebit.payments.dao;

import uk.gov.pay.directdebit.payments.params.PaginationParams;

import java.util.HashMap;
import java.util.Map;

public class PaymentViewSearchParams {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_FIELD = "gatewayAccountExternalId";
    private static final String PAGE_NUMBER_FIELD = "offset";
    private static final String PAGE_SIZE_FIELD = "limit";
    private String gatewayExternalId;
    private Long page;
    private Long displaySize;
    private PaginationParams paginationParams;
    private Map<String, Object> queryMap;

    public PaymentViewSearchParams(String gatewayExternalId, Long page, Long displaySize) {
        this.gatewayExternalId = gatewayExternalId;
        this.page = page;
        this.displaySize = displaySize;
    }

    public String getGatewayExternalId() {
        return gatewayExternalId;
    }

    public Long getPage() {
        return page;
    }

    public Long getDisplaySize() {
        return displaySize;
    }

    public PaginationParams getPaginationParams() {
        if (paginationParams == null) {
            return new PaginationParams(page, displaySize);
        }
        return paginationParams;
    }

    public void setPaginationParams(PaginationParams paginationParams) {
        this.paginationParams = paginationParams;
    }

    public String generateQuery() {
        return "";
    }

    public Map<String, Object> getQueryMap() {
        if (queryMap == null) {
            queryMap = new HashMap<>();
            queryMap.put(GATEWAY_ACCOUNT_EXTERNAL_FIELD, gatewayExternalId);
            queryMap.put(PAGE_NUMBER_FIELD, getPaginationParams().getPageNumber());
            queryMap.put(PAGE_SIZE_FIELD, getPaginationParams().getDisplaySize());
        }
        return queryMap;
    }
}
