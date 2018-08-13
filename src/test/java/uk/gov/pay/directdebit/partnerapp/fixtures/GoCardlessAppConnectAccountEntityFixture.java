package uk.gov.pay.directdebit.partnerapp.fixtures;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;

public final class GoCardlessAppConnectAccountEntityFixture implements DbFixture<GoCardlessAppConnectAccountEntityFixture, GoCardlessAppConnectAccountEntity> {
    private Long id;
    private String token = RandomIdGenerator.newId();
    private Long gatewayAccountId;
    private Boolean active;
    private String redirectUri = "https://example.com/oauth/complete";

    private GoCardlessAppConnectAccountEntityFixture() {
    }

    public static GoCardlessAppConnectAccountEntityFixture aPartnerAppAccountFixture() {
        return new GoCardlessAppConnectAccountEntityFixture();
    }

    public GoCardlessAppConnectAccountEntityFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public GoCardlessAppConnectAccountEntityFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public GoCardlessAppConnectAccountEntityFixture withGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
        return this;
    }

    public GoCardlessAppConnectAccountEntityFixture withActive(Boolean active) {
        this.active = active;
        return this;
    }

    public GoCardlessAppConnectAccountEntityFixture withRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    @Override
    public GoCardlessAppConnectAccountEntity toEntity() {
        GoCardlessAppConnectAccountEntity goCardlessAppConnectAccountEntity = new GoCardlessAppConnectAccountEntity();
        goCardlessAppConnectAccountEntity.setId(id);
        goCardlessAppConnectAccountEntity.setToken(token);
        goCardlessAppConnectAccountEntity.setGatewayAccountId(gatewayAccountId);
        goCardlessAppConnectAccountEntity.setActive(active);
        goCardlessAppConnectAccountEntity.setRedirectUri(redirectUri);

        return goCardlessAppConnectAccountEntity;
    }

    @Override
    public GoCardlessAppConnectAccountEntityFixture insert(Jdbi jdbi) {
        jdbi.withHandle(handle ->
                handle.execute("INSERT INTO \n" +
                                "  gocardless_partner_app_account_connect_tokens(gateway_account_id, token, redirect_uri) \n" +
                                "  VALUES (?, ?, ?)",
                        gatewayAccountId, token, redirectUri));

        return this;
    }
}
