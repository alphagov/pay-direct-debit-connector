package uk.gov.pay.directdebit.partnerapp.fixtures;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectTokenEntity;

public final class GoCardlessAppConnectTokenEntityFixture implements DbFixture<GoCardlessAppConnectTokenEntityFixture, GoCardlessAppConnectTokenEntity> {
    private Long id;
    private String token = RandomIdGenerator.newId();
    private Long gatewayAccountId;
    private Boolean active;
    private String redirectUri = "https://example.com/oauth/complete";

    private GoCardlessAppConnectTokenEntityFixture() {
    }

    public static GoCardlessAppConnectTokenEntityFixture aPartnerAppTokenFixture() {
        return new GoCardlessAppConnectTokenEntityFixture();
    }

    public GoCardlessAppConnectTokenEntityFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public GoCardlessAppConnectTokenEntityFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public GoCardlessAppConnectTokenEntityFixture withGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
        return this;
    }

    public GoCardlessAppConnectTokenEntityFixture withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public GoCardlessAppConnectTokenEntityFixture withRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public GoCardlessAppConnectTokenEntity toEntity() {
        GoCardlessAppConnectTokenEntity goCardlessAppConnectTokenEntity = new GoCardlessAppConnectTokenEntity();
        goCardlessAppConnectTokenEntity.setId(id);
        goCardlessAppConnectTokenEntity.setToken(token);
        goCardlessAppConnectTokenEntity.setGatewayAccountId(gatewayAccountId);
        goCardlessAppConnectTokenEntity.setActive(active);
        goCardlessAppConnectTokenEntity.setRedirectUri(redirectUri);

        return goCardlessAppConnectTokenEntity;
    }

    @Override
    public GoCardlessAppConnectTokenEntityFixture insert(Jdbi jdbi) {
        jdbi.withHandle(handle ->
                handle.execute("INSERT INTO \n" +
                                "  gocardless_partner_app_account_connect_tokens(gateway_account_id, token, redirect_uri) \n" +
                                "  VALUES (?, ?, ?)",
                        gatewayAccountId, token, redirectUri));

        return this;
    }
}
