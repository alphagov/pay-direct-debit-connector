package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount.Type;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGatewayAccountRequest {

    @NotNull
    private final PaymentProvider paymentProvider;

    @NotEmpty
    @Size(min = 1, max = 50, message = "service_name must be between {min} and {max} characters.")
    private final String serviceName;

    @NotNull
    private final Type type;

    @Size(max = 255)
    private final String description;

    @Size(max = 255)
    private final String analyticsId;

    private final PaymentProviderAccessToken accessToken;
    private final PaymentProviderOrganisationIdentifier organisation;

    public CreateGatewayAccountRequest(@JsonProperty("payment_provider") PaymentProvider paymentProvider,
                                       @JsonProperty("service_name") String serviceName,
                                       @JsonProperty("type") Type type, 
                                       @JsonProperty("description") String description, 
                                       @JsonProperty("analytics_id") String analyticsId,
                                       @JsonProperty("access_token") PaymentProviderAccessToken accessToken,
                                       @JsonProperty("organisation") PaymentProviderOrganisationIdentifier organisation) {
        this.paymentProvider = paymentProvider;
        this.serviceName = serviceName;
        this.type = type;
        this.description = description;
        this.analyticsId = analyticsId;
        this.accessToken = accessToken;
        this.organisation = organisation;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getAnalyticsId() {
        return analyticsId;
    }

    public PaymentProviderAccessToken getAccessToken() {
        return accessToken;
    }

    public PaymentProviderOrganisationIdentifier getOrganisation() {
        return organisation;
    }
}
