package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class GatewayAccountResponse {
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;
    @JsonProperty("payment_provider")
    private String paymentProvider;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("analytics_id")
    private String analyticsId;
    @JsonProperty("links")
    private Map<String, Map<String, URI>> links = new HashMap<>();

    public GatewayAccountResponse(Long gatewayAccountId, String serviceName, String paymentProvider, String description, String type, String analyticsId) {
        this.gatewayAccountId = gatewayAccountId;
        this.serviceName = serviceName;
        this.paymentProvider = paymentProvider;
        this.description = description;
        this.type = type;
        this.analyticsId = analyticsId;
    }

    public static GatewayAccountResponse from(GatewayAccount gatewayAccount) {
        return new GatewayAccountResponse(
                gatewayAccount.getId(),
                gatewayAccount.getServiceName(),
                gatewayAccount.getPaymentProvider(),
                gatewayAccount.getDescription(),
                gatewayAccount.getType().toString(),
                gatewayAccount.getAnalyticsId()
        );
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

    public Map<String, Map<String, URI>> getLinks() {
        return links;
    }

    public void addLink(String key, URI uri) {
        links.put(key, ImmutableMap.of("href", uri));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GatewayAccountResponse that = (GatewayAccountResponse) o;
        return Objects.equal(gatewayAccountId, that.gatewayAccountId) &&
                Objects.equal(paymentProvider, that.paymentProvider) &&
                Objects.equal(description, that.description) &&
                Objects.equal(type, that.type) &&
                Objects.equal(analyticsId, that.analyticsId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(gatewayAccountId, paymentProvider, description, type, analyticsId);
    }
}

