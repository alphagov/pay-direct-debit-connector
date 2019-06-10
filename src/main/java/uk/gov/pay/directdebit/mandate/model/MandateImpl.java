package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class MandateImpl implements Mandate {
    private Long id;
    private final MandateExternalId externalId;
    private MandateState state;
    private final GatewayAccount gatewayAccount;
    private final String returnUrl;
    private MandateBankStatementReference mandateBankStatementReference;
    private final String serviceReference;
    private final ZonedDateTime createdDate;
    private Payer payer;
    private PaymentProviderMandateId paymentProviderMandateId;

    private MandateImpl(MandateBuilder builder) {
        this.id = builder.id;
        this.gatewayAccount = builder.gatewayAccount;
        this.externalId = builder.externalId;
        this.mandateBankStatementReference = builder.mandateBankStatementReference;
        this.serviceReference = builder.serviceReference;
        this.state = builder.state;
        this.returnUrl = builder.returnUrl;
        this.createdDate = builder.createdDate;
        this.payer = builder.payer;
        this.paymentProviderMandateId = builder.paymentProviderId;
    }

    @Override
    public Payer getPayer() {
        return payer;
    }

    @Override
    public GatewayAccount getGatewayAccount() {
        return gatewayAccount;
    }

    @Override
    public String getReturnUrl() {
        return returnUrl;
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public MandateExternalId getExternalId() {
        return externalId;
    }

    @Override
    public MandateState getState() {
        return state;
    }

    @Override
    public void setState(MandateState state) {
        this.state = state;
    }

    @Override
    public MandateBankStatementReference getMandateBankStatementReference() {
        return mandateBankStatementReference;
    }

    @Override
    public String getServiceReference() {
        return serviceReference;
    }

    public void setMandateBankStatementReference(MandateBankStatementReference mandateBankStatementReference) {
        this.mandateBankStatementReference = mandateBankStatementReference;
    }

    @Override
    public Optional<PaymentProviderMandateId> getPaymentProviderMandateId() {
        return Optional.ofNullable(paymentProviderMandateId);
    }

    public void setPaymentProviderMandateId(PaymentProviderMandateId paymentProviderMandateId) {
        this.paymentProviderMandateId = paymentProviderMandateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandateImpl mandate = (MandateImpl) o;
        return Objects.equals(id, mandate.id) &&
                Objects.equals(externalId, mandate.externalId) &&
                state == mandate.state &&
                Objects.equals(gatewayAccount, mandate.gatewayAccount) &&
                Objects.equals(returnUrl, mandate.returnUrl) &&
                Objects.equals(mandateBankStatementReference, mandate.mandateBankStatementReference) &&
                Objects.equals(serviceReference, mandate.serviceReference) &&
                Objects.equals(createdDate, mandate.createdDate) &&
                Objects.equals(payer, mandate.payer) &&
                Objects.equals(paymentProviderMandateId, mandate.paymentProviderMandateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalId, state, gatewayAccount, returnUrl,
                mandateBankStatementReference, serviceReference, createdDate, payer, paymentProviderMandateId);
    }


    public static final class MandateBuilder {
        private Long id;
        private MandateExternalId externalId;
        private MandateState state;
        private GatewayAccount gatewayAccount;
        private String returnUrl;
        private MandateBankStatementReference mandateBankStatementReference;
        private String serviceReference;
        private ZonedDateTime createdDate;
        private Payer payer;
        private PaymentProviderMandateId paymentProviderId;

        private MandateBuilder() {
        }

        public static MandateBuilder aMandate() {
            return new MandateBuilder();
        }

        public MandateBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public MandateBuilder withExternalId(MandateExternalId externalId) {
            this.externalId = externalId;
            return this;
        }

        public MandateBuilder withState(MandateState state) {
            this.state = state;
            return this;
        }

        public MandateBuilder withGatewayAccount(GatewayAccount gatewayAccount) {
            this.gatewayAccount = gatewayAccount;
            return this;
        }

        public MandateBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public MandateBuilder withMandateBankStatementReference(MandateBankStatementReference mandateReference) {
            this.mandateBankStatementReference = mandateReference;
            return this;
        }

        public MandateBuilder withServiceReference(String serviceReference) {
            this.serviceReference = serviceReference;
            return this;
        }

        public MandateBuilder withCreatedDate(ZonedDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public MandateBuilder withPayer(Payer payer) {
            this.payer = payer;
            return this;
        }

        public MandateBuilder withPaymentProviderId(PaymentProviderMandateId paymentProviderId) {
            this.paymentProviderId = paymentProviderId;
            return this;
        }

        public MandateImpl build() {
            return new MandateImpl(this);
        }
    }
}
