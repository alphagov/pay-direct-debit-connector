package uk.gov.pay.directdebit.gatewayaccounts.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount.Type;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGatewayAccountRequest {

    @NotNull
    private final PaymentProvider paymentProvider;

    @NotNull
    private final Type type;

    @Length(max = 255, message = "Field [description] must have a maximum length of {max}")
    private final String description;

    @Length(max = 255, message = "Field [analytics_id] must have a maximum length of {max}")
    private final String analyticsId;

    private final PaymentProviderAccessToken accessToken;
    private final GoCardlessOrganisationId organisation;

    public CreateGatewayAccountRequest(@JsonProperty("payment_provider") PaymentProvider paymentProvider,
                                       @JsonProperty("type") Type type, 
                                       @JsonProperty("description") String description, 
                                       @JsonProperty("analytics_id") String analyticsId,
                                       @JsonProperty("access_token") PaymentProviderAccessToken accessToken,
                                       @JsonProperty("organisation") GoCardlessOrganisationId organisation) {
        this.paymentProvider = paymentProvider;
        this.type = type;
        this.description = description;
        this.analyticsId = analyticsId;
        this.accessToken = accessToken;
        this.organisation = organisation;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
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

    public GoCardlessOrganisationId getOrganisation() {
        return organisation;
    }
}
