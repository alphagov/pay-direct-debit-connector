package uk.gov.pay.directdebit.partnerapp.fixtures;

import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessAppConnectAccessTokenResponse;

public final class GoCardlessAppConnectClientResponseBuilder {
    private PaymentProviderAccessToken accessToken = PaymentProviderAccessToken.of("some-access-token");
    private String scope = "some-scope";
    private String tokenType = "some-token-type";
    private GoCardlessOrganisationId organisationId = GoCardlessOrganisationId.valueOf("some-organisation-id");

    private GoCardlessAppConnectClientResponseBuilder() {
    }

    public static GoCardlessAppConnectClientResponseBuilder aGoCardlessConnectClientResponse() {
        return new GoCardlessAppConnectClientResponseBuilder();
    }

    public GoCardlessAppConnectClientResponseBuilder withAccessToken(PaymentProviderAccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public GoCardlessAppConnectClientResponseBuilder withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public GoCardlessAppConnectClientResponseBuilder withTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public GoCardlessAppConnectClientResponseBuilder withOrganisationId(GoCardlessOrganisationId organisationId) {
        this.organisationId = organisationId;
        return this;
    }

    public GoCardlessAppConnectAccessTokenResponse build() {
        GoCardlessAppConnectAccessTokenResponse goCardlessAppConnectAccessTokenResponse = new GoCardlessAppConnectAccessTokenResponse();
        goCardlessAppConnectAccessTokenResponse.setAccessToken(accessToken);
        goCardlessAppConnectAccessTokenResponse.setOrganisationId(organisationId);
        return goCardlessAppConnectAccessTokenResponse;
    }
}
