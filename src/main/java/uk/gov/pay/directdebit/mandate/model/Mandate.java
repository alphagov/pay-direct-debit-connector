package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class Mandate {
    private final String description;
    private final Long id;
    private final MandateExternalId externalId;
    private final MandateState state;
    private final GatewayAccount gatewayAccount;
    private final String returnUrl;
    private final MandateBankStatementReference mandateBankStatementReference;
    private final String serviceReference;
    private final ZonedDateTime createdDate;
    private final Payer payer;
    private final PaymentProviderMandateId paymentProviderMandateId;

    private Mandate(MandateBuilder builder) {
        this.id = builder.id;
        this.gatewayAccount = Objects.requireNonNull(builder.gatewayAccount);
        this.externalId = Objects.requireNonNull(builder.externalId);
        this.serviceReference = Objects.requireNonNull(builder.serviceReference);
        this.state = Objects.requireNonNull(builder.state);
        this.returnUrl = Objects.requireNonNull(builder.returnUrl);
        this.createdDate = Objects.requireNonNull(builder.createdDate);
        this.payer = builder.payer;
        this.mandateBankStatementReference = builder.mandateBankStatementReference;
        this.paymentProviderMandateId = builder.paymentProviderId;
        this.description = builder.description;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Payer getPayer() {
        return payer;
    }

    public GatewayAccount getGatewayAccount() {
        return gatewayAccount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public Long getId() {
        return id;
    }

    public MandateExternalId getExternalId() {
        return externalId;
    }

    public MandateState getState() {
        return state;
    }

    public MandateBankStatementReference getMandateBankStatementReference() {
        return mandateBankStatementReference;
    }

    public String getServiceReference() {
        return serviceReference;
    }

    public Optional<PaymentProviderMandateId> getPaymentProviderMandateId() {
        return Optional.ofNullable(paymentProviderMandateId);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mandate mandate = (Mandate) o;
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
        private String description;

        private MandateBuilder() {
        }

        public static MandateBuilder aMandate() {
            return new MandateBuilder();
        }

        public static MandateBuilder fromMandate(Mandate mandate) {
            return aMandate()
            .withId(mandate.id)
            .withGatewayAccount(mandate.gatewayAccount)
            .withExternalId(mandate.externalId)
            .withMandateBankStatementReference(mandate.mandateBankStatementReference)
            .withServiceReference(mandate.serviceReference)
            .withState(mandate.state)
            .withReturnUrl(mandate.returnUrl)
            .withCreatedDate(mandate.createdDate)
            .withPayer(mandate.payer)
            .withPaymentProviderId(mandate.paymentProviderMandateId)
            .withDescription(mandate.description);
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
        
        public MandateBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Mandate build() {
            return new Mandate(this);
        }
    }
}
