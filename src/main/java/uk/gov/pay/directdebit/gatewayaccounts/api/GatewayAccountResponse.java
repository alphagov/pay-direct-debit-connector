package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("payment_provider")
    private String paymentProvider;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("analytics_id")
    private String analyticsId;
    @JsonProperty("is_connected")
    private final boolean isConnected;
    @JsonProperty("links")
    private List<Map<String, Object>> links = new ArrayList<>();

    public GatewayAccountResponse(Long gatewayAccountId, 
                                  String gatewayAccountExternalId, 
                                  String paymentProvider, 
                                  String description, 
                                  String type, 
                                  String analyticsId, 
                                  boolean isConnected) {
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.paymentProvider = paymentProvider;
        this.description = description;
        this.type = type;
        this.analyticsId = analyticsId;
        this.isConnected = isConnected;
    }

    public static GatewayAccountResponse from(GatewayAccount gatewayAccount) {
        return new GatewayAccountResponse(
                gatewayAccount.getId(),
                gatewayAccount.getExternalId(),
                gatewayAccount.getPaymentProvider().toString(),
                gatewayAccount.getDescription(),
                gatewayAccount.getType().toString(),
                gatewayAccount.getAnalyticsId(),
                gatewayAccount.getAccessToken().isPresent());
    }

    public GatewayAccountResponse withSelfLink(UriInfo uriInfo) {
        this.links.add(createLink(
                "self",
                GET,
                selfUriFor(uriInfo, GATEWAY_ACCOUNT_API_PATH, gatewayAccountExternalId)));
        return this;
    }

    public URI getSelfLink() {
        return this.links.stream()
                .filter(link -> link.get("rel").equals("self"))
                .findFirst()
                .map(link -> (URI) link.get("href"))
                .orElse(URI.create(""));
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
}

