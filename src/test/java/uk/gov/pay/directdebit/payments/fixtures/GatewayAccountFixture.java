package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

public class GatewayAccountFixture implements DbFixture<GatewayAccountFixture, GatewayAccount> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private PaymentProvider paymentProvider = PaymentProvider.SANDBOX;
    private GatewayAccount.Type type = GatewayAccount.Type.TEST;
    private String serviceName = RandomStringUtils.randomAlphabetic(25);
    private String description = RandomStringUtils.randomAlphabetic(25);
    private String analyticsId = RandomStringUtils.randomAlphanumeric(25);
    private PaymentProviderAccessToken accessToken = PaymentProviderAccessToken.of(RandomStringUtils.randomAlphabetic(25));
    private PaymentProviderOrganisationIdentifier organisation = PaymentProviderOrganisationIdentifier.of(RandomStringUtils.randomAlphanumeric(25));

    private GatewayAccountFixture() {
    }

    public static GatewayAccountFixture aGatewayAccountFixture() {
        return new GatewayAccountFixture();
    }

    public GatewayAccountFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public GatewayAccountFixture withPaymentProvider(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
        return this;
    }

    public GatewayAccountFixture withType(GatewayAccount.Type type) {
        this.type = type;
        return this;
    }

    public GatewayAccountFixture withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public GatewayAccountFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public GatewayAccountFixture withAnalyticsId(String analyticsId) {
        this.analyticsId = analyticsId;
        return this;
    }

    public GatewayAccountFixture withAccessToken(PaymentProviderAccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public GatewayAccountFixture withOrganisation(PaymentProviderOrganisationIdentifier organisation) {
        this.organisation = organisation;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }

    public GatewayAccount.Type getType() {
        return type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public String getAnalyticsId() {
        return analyticsId;
    }

    public PaymentProviderOrganisationIdentifier getOrganisation() { return organisation; }

    @Override
    public GatewayAccountFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    gateway_accounts(\n" +
                                "        id,\n" +
                                "        external_id,\n" +
                                "        payment_provider," +
                                "        service_name,\n" +
                                "        type,\n" +
                                "        description,\n" +
                                "        analytics_id,\n" +
                                "        access_token,\n" +
                                "        organisation\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        externalId,
                        paymentProvider.toString(),
                        serviceName,
                        type.toString(),
                        description,
                        analyticsId,
                        accessToken != null ? accessToken.toString() : null,
                        organisation != null ? organisation.toString() : null
                )
        );
        return this;
    }

    @Override
    public GatewayAccount toEntity() {
        return new GatewayAccount(id, externalId, paymentProvider, type, serviceName, description, analyticsId, accessToken, organisation);
    }

}
