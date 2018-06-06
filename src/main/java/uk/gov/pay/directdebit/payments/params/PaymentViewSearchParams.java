package uk.gov.pay.directdebit.payments.params;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class PaymentViewSearchParams {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_FIELD = "gatewayAccountExternalId";
    private static final String PAGE_NUMBER_FIELD = "offset";
    private static final String PAGE_SIZE_FIELD = "limit";
    private static final String FROM_DATE_FIELD = "fromDate";
    private static final String TO_DATE_FIELD = "toDate";
    private static final String EMAIL_FIELD = "email";
    private static final String REFERENCE_FIELD = "reference";
    private static final String AMOUNT_FIELD = "amount";
    private final String gatewayExternalId;
    private final Long page;
    private final Long displaySize;
    private final String fromDateString;
    private final String toDateString;
    private final String email;
    private final String reference;
    private final Long amount;
    private final SearchDateParams searchDateParams;
    private final PaginationParams paginationParams;
    private Map<String, Object> queryMap;

    public PaymentViewSearchParams(String gatewayExternalId, Long page, Long displaySize, 
                                   String fromDateString, String toDateString, String email, String reference, Long amount) {
        this(gatewayExternalId, page, displaySize, fromDateString, toDateString, email, reference, amount,
                null, null);
    }
    
    public PaymentViewSearchParams(String gatewayExternalId, Long page, Long displaySize, String fromDateString, String toDateString,
                                   String email, String reference, Long amount, 
                                   PaginationParams paginationParams, SearchDateParams searchDateParams) {
        this.gatewayExternalId = gatewayExternalId;
        this.page = page;
        this.displaySize = displaySize;
        this.fromDateString = fromDateString;
        this.toDateString = toDateString;
        this.email = email;
        this.reference = reference;
        this.amount = amount;
        this.searchDateParams = searchDateParams;
        this.paginationParams = paginationParams;
    }

    public String getGatewayExternalId() { return gatewayExternalId; }

    public Long getPage() { return page; }

    public Long getDisplaySize() { return displaySize; }

    public String getFromDateString() { return fromDateString; }

    public String getToDateString() { return toDateString; }

    public String getEmail() { return email; }

    public String getReference() { return reference; }

    public Long getAmount() { return amount; }

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
            sb.append(" AND t.created_date > :" + FROM_DATE_FIELD);
        }
        if (searchDateParams.getToDate() != null) {
            sb.append(" AND t.created_date < :" + TO_DATE_FIELD);
        }
        if (isNotBlank(email)) {
            sb.append(" AND pa.email ILIKE :" + EMAIL_FIELD);
        }
        if (isNotBlank(reference)) {
            sb.append(" AND t.reference ILIKE :" + REFERENCE_FIELD);
        }
        if (amount != null) {
            sb.append(" AND t.amount = :" + AMOUNT_FIELD);
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
            if (isNotBlank(email)) {
                queryMap.put(EMAIL_FIELD, likeClause(email));
            }
            if (isNotBlank(reference)) {
                queryMap.put(REFERENCE_FIELD, likeClause(reference));
            }
            if (amount != null) {
                queryMap.put(AMOUNT_FIELD, amount);
            }
        }
        return queryMap;
    }

    private String likeClause(String rawUserInputText) {
        return "%" + rawUserInputText + "%";
    }
}
