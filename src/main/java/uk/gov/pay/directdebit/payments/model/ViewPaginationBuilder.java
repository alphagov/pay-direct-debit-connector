package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.api.PaymentViewResultResponse;
import uk.gov.pay.directdebit.payments.links.PaginationLink;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

public class ViewPaginationBuilder {

    private static final String SELF_LINK = "self";
    private static final String FIRST_LINK = "first_page";
    private static final String LAST_LINK = "last_page";
    private static final String PREV_LINK = "prev_page";
    private static final String NEXT_LINK = "next_page";
    private PaymentViewSearchParams searchParams;
    private UriInfo uriInfo;
    private List<PaymentViewResultResponse> viewResponses;

    @JsonIgnore
    private Long totalCount;
    @JsonIgnore
    private Long selfPageNum;
    @JsonProperty(SELF_LINK)
    private PaginationLink selfLink;
    @JsonProperty(FIRST_LINK)
    private PaginationLink firstLink;
    @JsonProperty(LAST_LINK)
    private PaginationLink lastLink;
    @JsonProperty(PREV_LINK)
    private PaginationLink prevLink;
    @JsonProperty(NEXT_LINK)
    private PaginationLink nextLink;

    public ViewPaginationBuilder(PaymentViewSearchParams searchParams, List<PaymentViewResultResponse> chargeResponses, UriInfo uriInfo) {
        this.searchParams = searchParams;
        this.viewResponses = chargeResponses;
        this.uriInfo = uriInfo;
        selfPageNum = searchParams.getPage();
    }

    public ViewPaginationBuilder withTotalCount(Long total) {
        this.totalCount = total;
        return this;
    }

    public ViewPaginationBuilder buildResponse() {
        Long size = searchParams.getDisplaySize();
        long lastPage = totalCount > 0 ? (totalCount + size - 1) / size : 1;
        buildLinks(lastPage);
        
        return this;
    }

    public Long getTotalCount() { return totalCount; }

    public Long getSelfPageNum() { return selfPageNum; }

    public PaginationLink getSelfLink() { return selfLink; }

    public PaginationLink getFirstLink() { return firstLink; }

    public PaginationLink getLastLink() { return lastLink; }

    public PaginationLink getPrevLink() { return prevLink; }

    public PaginationLink getNextLink() { return nextLink; }

    private void buildLinks(long lastPage) {
        selfLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());
        
        searchParams.withPage(1L);
        firstLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());

        searchParams.withPage(lastPage);
        lastLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());

        searchParams.withPage(selfPageNum - 1);
        prevLink = selfPageNum == 1L ? null : PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());;

        searchParams.withPage(selfPageNum + 1);
        nextLink = selfPageNum == lastPage ? null : PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());;
    }

    private URI uriWithParams(String params) {
        URI uri = uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPath())
                .replaceQuery(params)
                .build(searchParams.getGatewayExternalId());
        return uri;
    }
}
