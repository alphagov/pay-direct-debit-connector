package uk.gov.pay.directdebit.payments.params;

import java.util.HashMap;
import java.util.Map;

public class PaymentViewSearchParams {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_FIELD = "gatewayAccountExternalId";
    private static final String PAGE_NUMBER_FIELD = "offset";
    private static final String PAGE_SIZE_FIELD = "limit";
    private static final String FROM_DATE_FIELD = "fromDate";
    private static final String TO_DATE_FIELD = "toDate";
    private String gatewayExternalId;
    private Long page;
    private Long displaySize;
    private String fromDateString;
    private String toDateString;
    private SearchDateParams searchDateParams;
    private PaginationParams paginationParams;
    private Map<String, Object> queryMap;

    public PaymentViewSearchParams(String gatewayExternalId, Long page, Long displaySize, String fromDateString, String toDateString) {
        this(gatewayExternalId, page, displaySize, fromDateString, toDateString,
                null, null);
    }
    
    public PaymentViewSearchParams(String gatewayExternalId, Long page, Long displaySize, String fromDateString, String toDateString,
                                   PaginationParams paginationParams, SearchDateParams searchDateParams) {
        this.gatewayExternalId = gatewayExternalId;
        this.page = page;
        this.displaySize = displaySize;
        this.fromDateString = fromDateString;
        this.toDateString = toDateString;
        this.searchDateParams = searchDateParams;
        this.paginationParams = paginationParams;
    }

    public String getGatewayExternalId() { return gatewayExternalId; }

    public Long getPage() { return page; }

    public Long getDisplaySize() { return displaySize; }

    public String getFromDateString() { return fromDateString; }

    public String getToDateString() { return toDateString; }

    public PaginationParams getPaginationParams() {
        if (paginationParams == null) {
            return new PaginationParams(page, displaySize);
        }
        return paginationParams;
    }

    public SearchDateParams getSearchDateParams() { return searchDateParams; }

    public String generateQuery() {
        StringBuilder sb = new StringBuilder("");
        if (searchDateParams.getFromDate() != null) {
            sb.append("AND pr.created_date > :" + FROM_DATE_FIELD + " ");
        }
        if (searchDateParams.getToDate() != null) {
            sb.append("AND pr.created_date < :" + TO_DATE_FIELD + " ");
        }
        return sb.toString();
    }

    public Map<String, Object> getQueryMap() {
        if (queryMap == null) {
            queryMap = new HashMap<>();
            queryMap.put(GATEWAY_ACCOUNT_EXTERNAL_FIELD, gatewayExternalId);
            queryMap.put(PAGE_NUMBER_FIELD, getPaginationParams().getPageNumber());
            queryMap.put(PAGE_SIZE_FIELD, getPaginationParams().getDisplaySize());
            if (searchDateParams.getFromDate() != null) {
                queryMap.put(FROM_DATE_FIELD, searchDateParams.getFromDate());
            }
            if (searchDateParams.getToDate() != null) {
                queryMap.put(TO_DATE_FIELD, searchDateParams.getToDate());
            }
        }
        return queryMap;
    }
}
