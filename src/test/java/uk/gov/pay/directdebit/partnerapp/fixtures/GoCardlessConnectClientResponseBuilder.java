package uk.gov.pay.directdebit.partnerapp.fixtures;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectAccessTokenResponse;

public final class GoCardlessConnectClientResponseBuilder {
    private PaymentProviderAccessToken accessToken = PaymentProviderAccessToken.of("some-access-token");
    private String scope = "some-scope";
    private String tokenType = "some-token-type";
    private PaymentProviderOrganisationIdentifier organisationId = PaymentProviderOrganisationIdentifier.of("some-organisation-id");
    private String email = "org@example.com";

    private GoCardlessConnectClientResponseBuilder() {
    }

    public static GoCardlessConnectClientResponseBuilder aGoCardlessConnectClientResponse() {
        return new GoCardlessConnectClientResponseBuilder();
    }

    public GoCardlessConnectClientResponseBuilder withAccessToken(PaymentProviderAccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public GoCardlessConnectClientResponseBuilder withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public GoCardlessConnectClientResponseBuilder withTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    public GoCardlessConnectClientResponseBuilder withOrganisationId(PaymentProviderOrganisationIdentifier organisationId) {
        this.organisationId = organisationId;
        return this;
    }

    public GoCardlessConnectClientResponseBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public GoCardlessConnectAccessTokenResponse build() {
        GoCardlessConnectAccessTokenResponse goCardlessConnectAccessTokenResponse = new GoCardlessConnectAccessTokenResponse();
        goCardlessConnectAccessTokenResponse.setAccessToken(accessToken);
        goCardlessConnectAccessTokenResponse.setOrganisationId(organisationId);
        goCardlessConnectAccessTokenResponse.setEmail(email);
        return goCardlessConnectAccessTokenResponse;
    }
}
