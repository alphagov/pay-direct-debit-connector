package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.api.PaymentViewListResponse;
import uk.gov.pay.directdebit.payments.links.Link;
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
    private List<PaymentViewListResponse> viewResponses;

    private Long totalCount;
    private Long selfPageNum;
    @JsonProperty(SELF_LINK)
    private Link selfLink;
    @JsonProperty(FIRST_LINK)
    private Link firstLink;
    @JsonProperty(LAST_LINK)
    private Link lastLink;
    @JsonProperty(PREV_LINK)
    private Link prevLink;
    @JsonProperty(NEXT_LINK)
    private Link nextLink;

    public ViewPaginationBuilder(PaymentViewSearchParams searchParams, List<PaymentViewListResponse> chargeResponses, UriInfo uriInfo) {
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

    public Link getSelfLink() { return selfLink; }

    public Link getFirstLink() { return firstLink; }

    public Link getLastLink() { return lastLink; }

    public Link getPrevLink() { return prevLink; }

    public Link getNextLink() { return nextLink; }

    private void buildLinks(long lastPage) {
        selfLink = Link.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString(), "GET", SELF_LINK);
        
        searchParams.withPage(1L);
        firstLink = Link.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString(), "GET", FIRST_LINK);

        searchParams.withPage(lastPage);
        lastLink = Link.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString(), "GET", LAST_LINK);

        searchParams.withPage(selfPageNum - 1);
        prevLink = selfPageNum == 1L ? null : Link.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString(), "GET", PREV_LINK);;

        searchParams.withPage(selfPageNum + 1);
        nextLink = selfPageNum == lastPage ? null : Link.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString(), "GET", LAST_LINK);;
    }

    private URI uriWithParams(String params) {
        URI uri = uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPath())
                .replaceQuery(params)
                .build(searchParams.getGatewayExternalId());
        return uri;
    }
}
