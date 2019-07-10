package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.common.model.SearchParams;
import uk.gov.pay.directdebit.payments.links.PaginationLink;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class LinksForSearchResult {

    private final SearchParams searchParams;
    private final UriInfo uriInfo;
    private final Integer totalCount;
    private final String queryForSelfLink;
    private final Integer pageNumberForSelfLink;
    private final Integer lastPageNumber;
    private final Integer previousPageNumber;

    @JsonProperty("self")
    private PaginationLink selfLink;
    @JsonProperty("first_page")
    private PaginationLink firstLink;
    @JsonProperty("last_page")
    private PaginationLink lastLink;
    @JsonProperty("prev_page")
    private PaginationLink prevLink;
    @JsonProperty("next_page")
    private PaginationLink nextLink;

    public LinksForSearchResult(SearchParams searchParams, UriInfo uriInfo, Integer totalCount) {
        this.uriInfo = uriInfo;
        this.totalCount = totalCount;
        this.searchParams = searchParams;
        this.queryForSelfLink = searchParams.buildQueryParamString();
        this.pageNumberForSelfLink = searchParams.getPage();
        this.lastPageNumber = calculateLastPageNumber();
        this.previousPageNumber = calculatePreviousPageNumber();
        buildLinks();
    }

    public PaginationLink getSelfLink() { return selfLink; }

    public PaginationLink getFirstLink() { return firstLink; }

    public PaginationLink getLastLink() { return lastLink; }

    public PaginationLink getPrevLink() { return prevLink; }

    public PaginationLink getNextLink() { return nextLink; }

    private void buildLinks() {
        selfLink = createLinkWithQueryOf(queryForSelfLink);
        firstLink = createLinkWithQueryOf(queryWithPageNumberOf(1));
        lastLink = createLinkWithQueryOf(queryWithPageNumberOf(lastPageNumber));

        if (notOnFirstPage()) {
            prevLink = createLinkWithQueryOf(queryWithPageNumberOf(previousPageNumber));
        }

        if (notOnLastPage()) {
            nextLink = createLinkWithQueryOf(queryWithPageNumberOf(pageNumberForSelfLink + 1));
        }
    }

    private boolean notOnFirstPage() {
        return pageNumberForSelfLink > 1;
    }

    private boolean notOnLastPage() {
        return pageNumberForSelfLink < lastPageNumber;
    }

    private int calculateLastPageNumber() {
        return totalCount > 0 ? (totalCount + searchParams.getDisplaySize() - 1) / searchParams.getDisplaySize() : 1;
    }

    private int calculatePreviousPageNumber() {
        return pageNumberForSelfLink > lastPageNumber ? lastPageNumber : pageNumberForSelfLink - 1;
    }

    private String queryWithPageNumberOf(Integer newPageNumber) {
        return queryForSelfLink.replace("page=" + pageNumberForSelfLink, "page=" + newPageNumber);
    }

    private PaginationLink createLinkWithQueryOf(String query) {
        return PaginationLink.ofValue(uriWithParams(query).toString());
    }

    private URI uriWithParams(String params) {
        return uriInfo.getBaseUriBuilder()
                .path(uriInfo.getPath())
                .replaceQuery(params)
                .build(searchParams.getGatewayExternalId());
    }
}
