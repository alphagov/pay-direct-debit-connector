package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.ViewPaginationBuilder;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResponse {

    @JsonIgnore
    private String gatewayExternalId;
    @JsonProperty("total")
    private Long total;
    @JsonProperty("count")
    private Long count;
    @JsonProperty("page")
    private Long page;
    @JsonProperty("results")
    private List<PaymentResponse> paymentViewResponses;
    @JsonProperty("_links")
    private ViewPaginationBuilder paginationBuilder;

    public PaymentViewResponse(String gatewayExternalId, Long total, Long page, List<PaymentResponse> paymentViewResponses) {
        this.gatewayExternalId = gatewayExternalId;
        this.total = total;
        this.count = (long)paymentViewResponses.size();
        this.page = page;
        this.paymentViewResponses = paymentViewResponses;
    }

    public List<PaymentResponse> getPaymentViewResponses() {
        return paymentViewResponses;
    }

    public Long getTotal() { return total; }

    public Long getCount() { return count; }

    public Long getPage() { return page; }

    public ViewPaginationBuilder getPaginationBuilder() { return paginationBuilder; }

    public PaymentViewResponse withPaginationBuilder(ViewPaginationBuilder paginationBuilder) {
        this.paginationBuilder = paginationBuilder;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentViewResponse that = (PaymentViewResponse) o;
        return Objects.equals(gatewayExternalId, that.gatewayExternalId) &&
                Objects.equals(total, that.total) &&
                Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayExternalId, total, page);
    }

    @Override
    public String toString() {
        return format("PaymentResponse{gatewayExternalId='%s', page='%s', total='%s', paymentViewResponses='%s'}"
                        + gatewayExternalId, page, total, paymentViewResponses.toString());
    }
}
