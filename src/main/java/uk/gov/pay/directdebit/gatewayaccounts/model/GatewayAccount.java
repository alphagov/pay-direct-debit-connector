package uk.gov.pay.directdebit.gatewayaccounts.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.exception.InvalidGatewayAccountException;

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

    public GatewayAccount(Long id, String externalId, PaymentProvider paymentProvider, Type type, String serviceName, String description, String analyticsId) {
        this.id = id;
        this.externalId = externalId;
        this.paymentProvider = paymentProvider;
        this.type = type;
        this.serviceName = serviceName;
        this.description = description;
        this.analyticsId = analyticsId;
    }

    public GatewayAccount(PaymentProvider paymentProvider, Type type, String serviceName, String description, String analyticsId) {
        this(null, generateExternalId(), paymentProvider, type, serviceName, description, analyticsId);
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
}
