package uk.gov.pay.directdebit.gatewayaccounts.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidGatewayAccountException;

import java.util.Optional;

public class GatewayAccount {

    public enum Type {
        TEST, LIVE;

        public static Type fromString(String type) {
            for (Type typeEnum : Type.values()) {
                if (typeEnum.toString().equalsIgnoreCase(type)) {
                    return typeEnum;
                }
            }
            throw new InvalidGatewayAccountException(type);
        }
    }
    private Long id;
    private String externalId;
    private PaymentProvider paymentProvider;
    private Type type;
    private String serviceName;
    private String description;
    private String analyticsId;
    private PaymentProviderAccessToken accessToken;
    private PaymentProviderOrganisationIdentifier organisation;

    public GatewayAccount(Long id, String externalId, PaymentProvider paymentProvider, Type type, String serviceName, String description, String analyticsId,
                          PaymentProviderAccessToken accessToken, PaymentProviderOrganisationIdentifier organisation) {
        this.id = id;
        this.externalId = externalId;
        this.paymentProvider = paymentProvider;
        this.type = type;
        this.serviceName = serviceName;
        this.description = description;
        this.analyticsId = analyticsId;
        this.accessToken = accessToken;
        this.organisation = organisation;
    }

    public GatewayAccount(Long id, String externalId, PaymentProvider paymentProvider, Type type, String serviceName, String description, String analyticsId) {
        this(id, externalId, paymentProvider, type, serviceName, description, analyticsId, null, null);
    }
    
    public GatewayAccount(PaymentProvider paymentProvider, Type type, String serviceName, String description, String analyticsId) {
        this(null, generateExternalId(), paymentProvider, type, serviceName, description, analyticsId, null, null);
    }

    private static String generateExternalId() {
        return "DIRECT_DEBIT:" + RandomIdGenerator.newId();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public Type getType() {
        return type;
    }

    public GatewayAccount setType(Type type) {
        this.type = type;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public GatewayAccount setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GatewayAccount setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAnalyticsId() {
        return analyticsId;
    }

    public GatewayAccount setAnalyticsId(String analyticsId) {
        this.analyticsId = analyticsId;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public GatewayAccount setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Optional<PaymentProviderAccessToken> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public GatewayAccount setAccessToken(PaymentProviderAccessToken accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public Optional<PaymentProviderOrganisationIdentifier> getOrganisation() {
        return Optional.ofNullable(organisation);
    }

    public GatewayAccount setOrganisation(PaymentProviderOrganisationIdentifier organisation) {
        this.organisation = organisation;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GatewayAccount that = (GatewayAccount) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (!externalId.equals(that.externalId)) {
            return false;
        }
        if (paymentProvider != that.paymentProvider) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (serviceName != null ? !serviceName.equals(that.serviceName)
                : that.serviceName != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description)
                : that.description != null) {
            return false;
        }
        if (accessToken != null ? !accessToken.equals(that.accessToken)
                : that.accessToken != null) {
            return false;
        }
        if (organisation != null ? !organisation.equals(that.organisation)
                : that.organisation != null) {
            return false;
        }
        return analyticsId != null ? analyticsId.equals(that.analyticsId)
                : that.analyticsId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + externalId.hashCode();
        result = 31 * result + paymentProvider.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (analyticsId != null ? analyticsId.hashCode() : 0);
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        result = 31 * result + (organisation != null ? organisation.hashCode() : 0);

        return result;
    }
}
