package uk.gov.pay.directdebit.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.LinksForSearchResult;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class SearchResponse<T> {

    @JsonIgnore
    private final String gatewayExternalId;
    @JsonProperty("total")
    private final Integer total;
    @JsonProperty("count")
    private final Integer count;
    @JsonProperty("page")
    private final Integer page;
    @JsonProperty("results")
    private final List<T> results;
    @JsonProperty("_links")
    private final LinksForSearchResult linksForSearchResult;

    public SearchResponse(String gatewayExternalId, Integer total, Integer page, List<T> results, LinksForSearchResult linksForSearchResult) {
        this.gatewayExternalId = gatewayExternalId;
        this.total = total;
        this.count = results.size();
        this.page = page;
        this.results = results;
        this.linksForSearchResult = linksForSearchResult;
    }

    public List<T> getResults() {
        return results;
    }

    public Integer getTotal() { return total; }

    public Integer getCount() { return count; }

    public Integer getPage() { return page; }

    public LinksForSearchResult getLinksForSearchResult() { return linksForSearchResult; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchResponse<?> that = (SearchResponse<?>) o;
        return count.equals(that.count) &&
                Objects.equals(gatewayExternalId, that.gatewayExternalId) &&
                Objects.equals(total, that.total) &&
                Objects.equals(page, that.page) &&
                Objects.equals(results, that.results) &&
                Objects.equals(linksForSearchResult, that.linksForSearchResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayExternalId, total, count, page, results, linksForSearchResult);
    }

    @Override
    public String toString() {
        return format("SearchResponse{gatewayExternalId='%s', page='%s', total='%s', results='%s'}"
                        + gatewayExternalId, page, total, results.toString());
    }
}
