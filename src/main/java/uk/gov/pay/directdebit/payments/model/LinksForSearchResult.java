package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.links.PaginationLink;
import uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;
import static uk.gov.pay.directdebit.payments.params.PaymentViewSearchParams.PaymentViewSearchParamsBuilder.fromPaymentViewParams;

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
    private final Integer totalCount;
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

    public LinksForSearchResult(PaymentViewSearchParams searchParams, UriInfo uriInfo, Integer totalCount) {
        this.searchParams = searchParams;
        this.uriInfo = uriInfo;
        this.totalCount = totalCount;
        buildLinks();
    }

    public PaginationLink getSelfLink() { return selfLink; }

    public PaginationLink getFirstLink() { return firstLink; }

    public PaginationLink getLastLink() { return lastLink; }

    public PaginationLink getPrevLink() { return prevLink; }

    public PaginationLink getNextLink() { return nextLink; }

    private void buildLinks() {
        int currentPageNumber = searchParams.getPage();
        int lastPageNumber = totalCount > 0 ? (totalCount + searchParams.getDisplaySize() - 1) / searchParams.getDisplaySize() : 1;
        
        selfLink = createLink(currentPageNumber);
        firstLink = createLink(1);
        lastLink = createLink(lastPageNumber);
        
        if (currentPageNumber > 1) {
            int previousPageNumber = currentPageNumber > lastPageNumber ? lastPageNumber : currentPageNumber - 1;
            prevLink = createLink(previousPageNumber);
        }
        
        if (currentPageNumber < lastPageNumber) {
            nextLink = createLink(currentPageNumber + 1);
        }
    }
    
    private PaginationLink createLink(Integer pageNumber) {
        var searchParamsForPage = fromPaymentViewParams(searchParams).withPage(pageNumber).build();
        return PaginationLink.ofValue(uriWithParams(searchParamsForPage.buildQueryParamString()).toString());
    }

    private URI uriWithParams(String params) {
        return uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPath())
                .replaceQuery(params)
                .build(searchParams.getGatewayExternalId());
    }
}
