package uk.gov.pay.directdebit.payments.params;

import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.directdebit.payments.model.PaymentState.CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.EXPIRED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentState.NEW;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentState.SUCCESS;
import static uk.gov.pay.directdebit.payments.model.PaymentState.USER_CANCEL_NOT_ELIGIBLE;

public class PaymentViewSearchParams {

    private static final String GATEWAY_ACCOUNT_EXTERNAL_FIELD = "gatewayAccountExternalId";
    private static final String PAGE_NUMBER_FIELD = "offset";
    private static final String PAGE_SIZE_FIELD = "limit";
    private static final String FROM_DATE_FIELD = "fromDate";
    private static final String TO_DATE_FIELD = "toDate";
    private static final String REFERENCE_FIELD = "reference";
    private static final String AMOUNT_FIELD = "amount";
    private static final String MANDATE_ID_INTERNAL_KEY = "mandate_id";
    private static final String MANDATE_ID_EXTERNAL_KEY = "agreement_id";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String STATE_FIELD = "state";
    
    private final String gatewayExternalId;
    private Long page;
    private Long displaySize;
    private String fromDateString;
    private String toDateString;
    private String reference;
    private Long amount;
    private String mandateId;
    private String state;
    private SearchDateParams searchDateParams;
    private PaginationParams paginationParams;
    private Map<String, Object> queryMap;

    private static Map<String, String> externalPaymentToInternalStateQueryMap = new HashMap<>(4);

    static {
        // see uk.gov.pay.directdebit.payments.model.PaymentState for mappings
        externalPaymentToInternalStateQueryMap
                .put(ExternalPaymentState.EXTERNAL_STARTED.getStatus(), NEW.toSingleQuoteString());
        externalPaymentToInternalStateQueryMap
                .put(ExternalPaymentState.EXTERNAL_PENDING.getStatus(), PENDING.toSingleQuoteString());
        externalPaymentToInternalStateQueryMap
                .put(ExternalPaymentState.EXTERNAL_CANCELLED_USER_NOT_ELIGIBLE.getStatus(), USER_CANCEL_NOT_ELIGIBLE.toSingleQuoteString());
        externalPaymentToInternalStateQueryMap
                .put(ExternalPaymentState.EXTERNAL_SUCCESS.getStatus(), SUCCESS.toSingleQuoteString());
        externalPaymentToInternalStateQueryMap
                .put(ExternalPaymentState.EXTERNAL_FAILED.getStatus(), FAILED.toSingleQuoteString() + ", " + CANCELLED.toSingleQuoteString() + ", " + EXPIRED.toSingleQuoteString());
    }

    public PaymentViewSearchParams(String gatewayExternalId) {
        this.gatewayExternalId = gatewayExternalId;
    }

    public PaymentViewSearchParams withDisplaySize(Long displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public PaymentViewSearchParams withFromDateString(String fromDateString) {
        this.fromDateString = fromDateString;
        return this;
    }

    public PaymentViewSearchParams withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentViewSearchParams withToDateString(String toDateString) {
        this.toDateString = toDateString;
        return this;
    }

    public PaymentViewSearchParams withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public PaymentViewSearchParams withMandateId(String mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public PaymentViewSearchParams withState(String state) {
        this.state = state;
        return this;
    }

    public PaymentViewSearchParams withPaginationParams(PaginationParams paginationParams) {
        this.paginationParams = paginationParams;
        return this;
    }

    public PaymentViewSearchParams withSearchDateParams(SearchDateParams searchDateParams) {
        this.searchDateParams = searchDateParams;
        return this;
    }

    public String getGatewayExternalId() { return gatewayExternalId; }

    public Long getPage() { return page; }

    public Long getDisplaySize() { return displaySize; }

    public String getFromDateString() { return fromDateString; }

    public String getToDateString() { return toDateString; }

    public String getReference() { return reference; }

    public Long getAmount() { return amount; }

    public String getMandateId() { return mandateId; }

    public String getState() { return state; }

    public PaginationParams getPaginationParams() {
        if (paginationParams == null) {
            return new PaginationParams(page, displaySize);
        }
        return paginationParams;
    }

    public SearchDateParams getSearchDateParams() { return searchDateParams; }
    
    public PaymentViewSearchParams withPage(Long page) {
        this.page = page;
        return this;
    }

    public String generateQuery() {
        StringBuilder sb = new StringBuilder();
        if (isNotBlank(mandateId)) {
            sb.append(" AND m.external_id = :" + MANDATE_ID_INTERNAL_KEY);
        }
        if (searchDateParams != null) {
            if (searchDateParams.getFromDate() != null) {
                sb.append(" AND p.created_date > :" + FROM_DATE_FIELD);
            }
            if (searchDateParams.getToDate() != null) {
                sb.append(" AND p.created_date < :" + TO_DATE_FIELD);
            }
        }
        if (isNotBlank(reference)) {
            sb.append(" AND p.reference ILIKE :" + REFERENCE_FIELD);
        }
        if (amount != null) {
            sb.append(" AND p.amount = :" + AMOUNT_FIELD);
        }
        if (isNotBlank(state) && externalPaymentToInternalStateQueryMap.containsKey(state)) {
            sb.append(format(" AND p.state IN(%s)", externalPaymentToInternalStateQueryMap.get(state)));
        }
        return sb.toString();
    }

    public Map<String, Object> getQueryMap() {
        if (queryMap == null) {
            queryMap = new HashMap<>();
            queryMap.put(GATEWAY_ACCOUNT_EXTERNAL_FIELD, gatewayExternalId);
            queryMap.put(PAGE_NUMBER_FIELD, getPaginationParams().getPageNumber());
            queryMap.put(PAGE_SIZE_FIELD, getPaginationParams().getDisplaySize());
            if (isNotBlank(mandateId)) {
                queryMap.put(MANDATE_ID_INTERNAL_KEY, mandateId);
            }
            if (searchDateParams != null) {
                if (searchDateParams.getFromDate() != null) {
                    queryMap.put(FROM_DATE_FIELD, searchDateParams.getFromDate());
                }
                if (searchDateParams.getToDate() != null) {
                    queryMap.put(TO_DATE_FIELD, searchDateParams.getToDate());
                }
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

    public String buildQueryParamString() {
        String query = "";
        if (isNotBlank(mandateId)) {
            query += "&" + MANDATE_ID_EXTERNAL_KEY + "=" + mandateId;
        }
        if (searchDateParams != null) {
            if (searchDateParams.getFromDate() != null) {
                query += "&" + FROM_DATE_KEY + "=" + searchDateParams.getFromDate().toString();
            }
            if (searchDateParams.getToDate() != null) {
                query += "&" + TO_DATE_KEY + "=" + searchDateParams.getToDate().toString();
            }
        }
        if (isNotBlank(reference)) {
            query += "&" + REFERENCE_FIELD + "=" + reference;
        }
        if (amount != null) {
            query += "&" + AMOUNT_FIELD + "=" + amount;
        }
        if (isNotBlank(state)) {
            query += "&" + STATE_FIELD + "=" + state;
        }
        query += addPaginationParams();
        return query.substring(1);
    }

    private String addPaginationParams() {
        String queryParams = format("&page=%s", page);
        queryParams += format("&display_size=%s", displaySize);
        return queryParams;
    }

    private String likeClause(String rawUserInputText) {
        return "%" + rawUserInputText + "%";
    }

}
