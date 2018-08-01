package uk.gov.pay.directdebit.partnerapp.fixtures;

import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;

public final class PartnerAppTokenEntityFixture implements DbFixture<PartnerAppTokenEntityFixture, PartnerAppTokenEntity> {
    private Long id;
    private String token = RandomIdGenerator.newId();
    private Long gatewayAccountId;
    private Boolean active;

    private PartnerAppTokenEntityFixture() {
    }

    public static PartnerAppTokenEntityFixture aPartnerAppTokenFixture() {
        return new PartnerAppTokenEntityFixture();
    }

    public PartnerAppTokenEntityFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public PartnerAppTokenEntityFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public PartnerAppTokenEntityFixture withGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
        return this;
    }

    public PartnerAppTokenEntityFixture withActive(Boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public PartnerAppTokenEntity toEntity() {
        PartnerAppTokenEntity partnerAppTokenEntity = new PartnerAppTokenEntity();
        partnerAppTokenEntity.setId(id);
        partnerAppTokenEntity.setToken(token);
        partnerAppTokenEntity.setGatewayAccountId(gatewayAccountId);
        partnerAppTokenEntity.setActive(active);
        return partnerAppTokenEntity;
    }

    @Override
    public PartnerAppTokenEntityFixture insert(Jdbi jdbi) {
        jdbi.withHandle(handle ->
                handle.execute("INSERT INTO \n" +
                                "  gocardless_partner_app_account_connect_tokens(gateway_account_id, token) \n" +
                                "  VALUES (?, ?)",
                        gatewayAccountId, token));

        return this;
    }
}
