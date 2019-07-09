package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.links.PaginationLink;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LinksForSearchResult {

    private static final String SELF_LINK = "self";
    private static final String FIRST_LINK = "first_page";
    private static final String LAST_LINK = "last_page";
    private static final String PREV_LINK = "prev_page";
    private static final String NEXT_LINK = "next_page";
    private final PaymentViewSearchParams searchParams;
    private final UriInfo uriInfo;

    @JsonIgnore
    private final Long totalCount;
    @JsonIgnore
    private final Long selfPageNum;
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

    public LinksForSearchResult(PaymentViewSearchParams searchParams, UriInfo uriInfo, Long totalCount) {
        this.searchParams = searchParams;
        this.uriInfo = uriInfo;
        this.selfPageNum = searchParams.getPage();
        this.totalCount = totalCount;
        buildLinks();
    }

    public PaginationLink getSelfLink() { return selfLink; }

    public PaginationLink getFirstLink() { return firstLink; }

    public PaginationLink getLastLink() { return lastLink; }

    public PaginationLink getPrevLink() { return prevLink; }

    public PaginationLink getNextLink() { return nextLink; }

    private void buildLinks() {
        Long size = searchParams.getDisplaySize();
        long lastPage = totalCount > 0 ? (totalCount + size - 1) / size : 1;
        
        selfLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());
        
        searchParams.withPage(1L);
        firstLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());

        searchParams.withPage(lastPage);
        lastLink = PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());

        searchParams.withPage(selfPageNum - 1);
        prevLink = selfPageNum == 1L ? null : 
                selfPageNum > lastPage ? 
                        PaginationLink.ofValue(uriWithParams(searchParams.withPage(lastPage).buildQueryParamString()).toString()) :
                        PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());

        searchParams.withPage(selfPageNum + 1);
        nextLink = selfPageNum >= lastPage ? null : PaginationLink.ofValue(uriWithParams(searchParams.buildQueryParamString()).toString());;
    }

    private URI uriWithParams(String params) {
        return uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPath())
                .replaceQuery(params)
                .build(searchParams.getGatewayExternalId());
    }
}
