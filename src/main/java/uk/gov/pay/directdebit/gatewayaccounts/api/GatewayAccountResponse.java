package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.HttpMethod.GET;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNT_API_PATH;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class GatewayAccountResponse {
    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;
    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;
    @JsonProperty("payment_method")
    private final String paymentMethod="DIRECT_DEBIT";
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("payment_provider")
    private String paymentProvider;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("analytics_id")
    private String analyticsId;
    @JsonProperty("links")
    private List<Map<String, Object>> links = new ArrayList<>();

    public GatewayAccountResponse(Long gatewayAccountId, String gatewayAccountExternalId, String serviceName, String paymentProvider, String description, String type, String analyticsId) {
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.serviceName = serviceName;
        this.paymentProvider = paymentProvider;
        this.description = description;
        this.type = type;
        this.analyticsId = analyticsId;
    }

    public static GatewayAccountResponse from(GatewayAccount gatewayAccount) {
        return new GatewayAccountResponse(
                gatewayAccount.getId(),
                gatewayAccount.getExternalId(),
                gatewayAccount.getServiceName(),
                gatewayAccount.getPaymentProvider().toString(),
                gatewayAccount.getDescription(),
                gatewayAccount.getType().toString(),
                gatewayAccount.getAnalyticsId()
        );
    }

    public GatewayAccountResponse withSelfLink(UriInfo uriInfo) {
        this.links.add(createLink(
                "self",
                GET,
                selfUriFor(uriInfo, GATEWAY_ACCOUNT_API_PATH, gatewayAccountId.toString())));
        return this;
    }

    public URI getSelfLink() {
        return this.links.stream()
                .filter(link -> link.get("rel").equals("self"))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .orElse(URI.create(""));
    }
    public String getServiceName() {
        return serviceName;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getAnalyticsId() {
        return analyticsId;
    }

    public List<Map<String, Object>> getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayAccountResponse that = (GatewayAccountResponse) o;
        return Objects.equal(gatewayAccountId, that.gatewayAccountId) &&
                Objects.equal(gatewayAccountExternalId, that.gatewayAccountExternalId) &&
                Objects.equal(paymentProvider, that.paymentProvider) &&
                Objects.equal(description, that.description) &&
                Objects.equal(type, that.type) &&
                Objects.equal(analyticsId, that.analyticsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(gatewayAccountId, gatewayAccountExternalId, paymentProvider, description, type, analyticsId);
    }
}

