package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.directdebit.payments.model.ViewPaginationBuilder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResponse {

    @JsonProperty("gateway_account_external_id")
    private String gatewayExternalId;
    @JsonProperty("total")
    private Long total;
    @JsonProperty("count")
    private Long count;
    @JsonProperty("page")
    private Long page;
    @JsonProperty("results")
    private List<PaymentViewListResponse> paymentViewResponses;
    @JsonProperty("_links")
    private ViewPaginationBuilder paginationBuilder;

    public PaymentViewResponse(String gatewayExternalId, Long total, Long page, List<PaymentViewListResponse> paymentViewResponses) {
        this.gatewayExternalId = gatewayExternalId;
        this.total = total;
        this.count = (long)paymentViewResponses.size();
        this.page = page;
        this.paymentViewResponses = paymentViewResponses;
    }

    public List<PaymentViewListResponse> getPaymentViewResponses() {
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
    public int hashCode() {
        int result = gatewayExternalId.hashCode();
        result = 31 * result + page.hashCode();
        result = 31 * result + total.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                " gatewayExternalId='" + gatewayExternalId + '\n' +
                ", page='" + page + '\'' +
                ", total=" + total + '\'' +
                ", paymentViewResponses='" + paymentViewResponses.toString() + '\'' +
                "}";
    }
}
