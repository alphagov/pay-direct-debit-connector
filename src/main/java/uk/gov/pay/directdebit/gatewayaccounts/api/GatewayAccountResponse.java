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
import static uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse.GatewayAccountResponseBuilder.aGatewayAccountResponse;
import static uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource.GATEWAY_ACCOUNT_API_PATH;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class GatewayAccountResponse {
    @JsonProperty("gateway_account_id")
    private Long gatewayAccountId;
    @JsonProperty("gateway_account_external_id")
    private String gatewayAccountExternalId;
    @JsonProperty("payment_method")
    private String paymentMethod="DIRECT_DEBIT";
    @JsonProperty("payment_provider")
    private String paymentProvider;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("analytics_id")
    private String analyticsId;
    @JsonProperty("organisation")
    private String organisation;
    @JsonProperty("links")
    private List<Map<String, Object>> links = new ArrayList<>();
    @JsonProperty("is_connected")
    private final boolean isConnected;

    private GatewayAccountResponse(GatewayAccountResponseBuilder gatewayAccountBuilder) {
        this.gatewayAccountId = gatewayAccountBuilder.gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountBuilder.gatewayAccountExternalId;
        this.paymentProvider = gatewayAccountBuilder.paymentProvider;
        this.description = gatewayAccountBuilder.description;
        this.type = gatewayAccountBuilder.type;
        this.organisation = gatewayAccountBuilder.organisation;
        this.analyticsId = gatewayAccountBuilder.analyticsId;
        this.isConnected = gatewayAccountBuilder.isConnected;
    }

    public static GatewayAccountResponse from(GatewayAccount gatewayAccount) {
        GatewayAccountResponseBuilder builder = aGatewayAccountResponse()
                .withGatewayAccountId(gatewayAccount.getId())
                .withGatewayAccountExternalId(gatewayAccount.getExternalId())
                .withPaymentProvider(gatewayAccount.getPaymentProvider().toString())
                .withDescription(gatewayAccount.getDescription())
                .withType(gatewayAccount.getType().toString())
                .withAnalyticsId(gatewayAccount.getAnalyticsId())
                .withIsConnected(gatewayAccount.getAccessToken().isPresent());
        gatewayAccount.getOrganisation().ifPresent(organisation -> builder.withOrganisation(organisation.toString()));
        return builder.build();
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

    public static final class GatewayAccountResponseBuilder {
        private Long gatewayAccountId;
        private String gatewayAccountExternalId;
        private String paymentMethod="DIRECT_DEBIT";
        private String paymentProvider;
        private String description;
        private String type;
        private String analyticsId;
        private String organisation;
        private List<Map<String, Object>> links = new ArrayList<>();
        private boolean isConnected;

        private GatewayAccountResponseBuilder() {
        }

        public static GatewayAccountResponseBuilder aGatewayAccountResponse() {
            return new GatewayAccountResponseBuilder();
        }

        public GatewayAccountResponseBuilder withGatewayAccountId(Long gatewayAccountId) {
            this.gatewayAccountId = gatewayAccountId;
            return this;
        }

        public GatewayAccountResponseBuilder withGatewayAccountExternalId(String gatewayAccountExternalId) {
            this.gatewayAccountExternalId = gatewayAccountExternalId;
            return this;
        }

        public GatewayAccountResponseBuilder withPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public GatewayAccountResponseBuilder withPaymentProvider(String paymentProvider) {
            this.paymentProvider = paymentProvider;
            return this;
        }

        public GatewayAccountResponseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public GatewayAccountResponseBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public GatewayAccountResponseBuilder withAnalyticsId(String analyticsId) {
            this.analyticsId = analyticsId;
            return this;
        }

        public GatewayAccountResponseBuilder withOrganisation(String organisation) {
            this.organisation = organisation;
            return this;
        }

        public GatewayAccountResponseBuilder withLinks(List<Map<String, Object>> links) {
            this.links = links;
            return this;
        }

        public GatewayAccountResponseBuilder withIsConnected(boolean isConnected) {
            this.isConnected = isConnected;
            return this;
        }

        public GatewayAccountResponse build() {
            GatewayAccountResponse gatewayAccountResponse = new GatewayAccountResponse(this);
            gatewayAccountResponse.paymentMethod = this.paymentMethod;
            gatewayAccountResponse.links = this.links;
            return gatewayAccountResponse;
        }
    }
}

