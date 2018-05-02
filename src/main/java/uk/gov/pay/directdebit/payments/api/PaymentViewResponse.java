package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class PaymentViewResponse {

    @JsonProperty("gateway_account_external_id")
    private String gatewayExternalId;

    @JsonProperty("page")
    private Long page;

    @JsonProperty("display_size")
    private Long displaySize;

    @JsonProperty("payment_views")
    private List<PaymentViewListResponse> paymentViewResponses;

    public PaymentViewResponse(String gatewayExternalId, Long page, Long displaySize, List<PaymentViewListResponse> paymentViewResponses) {
        this.gatewayExternalId = gatewayExternalId;
        this.page = page;
        this.displaySize = displaySize;
        this.paymentViewResponses = paymentViewResponses;
    }

    @Override
    public int hashCode() {
        int result = gatewayExternalId.hashCode();
        result = 31 * result + page.hashCode();
        result = 31 * result + displaySize.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PaymentRequestResponse{" +
                " gatewayExternalId='" + gatewayExternalId + '\n' +
                ", page='" + page + '\'' +
                ", displaySize=" + displaySize + '\'' +
                ", paymentViewResponses='" + paymentViewResponses.toString() + '\'' +
                "}";
    }
}
