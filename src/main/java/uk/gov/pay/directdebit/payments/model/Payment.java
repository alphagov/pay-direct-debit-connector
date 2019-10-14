package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.mandate.model.Mandate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class Payment {

    private final Long id;
    private final String externalId;
    private final Long amount;
    private final PaymentState state;
    private final String stateDetails;
    private final String stateDetailsDescription;
    private final String description;
    private final String reference;
    private final PaymentProviderPaymentId providerId;
    private final ZonedDateTime createdDate;
    private final Mandate mandate;
    private final LocalDate chargeDate;

    private Payment(PaymentBuilder builder) {
        this.id = builder.id;
        this.externalId = Objects.requireNonNull(builder.externalId);
        this.amount = Objects.requireNonNull(builder.amount);
        this.state = Objects.requireNonNull(builder.state);
        this.stateDetails = builder.stateDetails;
        this.stateDetailsDescription = builder.stateDetailsDescription;
        this.description = builder.description;
        this.reference = Objects.requireNonNull(builder.reference);
        this.createdDate = Objects.requireNonNull(builder.createdDate);
        this.mandate = Objects.requireNonNull(builder.mandate);
        this.providerId = builder.providerId;
        this.chargeDate = builder.chargeDate;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentState getState() {
        return state;
    }

    public Optional<String> getStateDetails() {
        return Optional.ofNullable(stateDetails);
    }

    public Optional<String> getStateDetailsDescription() {
        return Optional.ofNullable(stateDetailsDescription);
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public Optional<PaymentProviderPaymentId> getProviderId() {
        return Optional.ofNullable(providerId);
    }

    public Optional<LocalDate> getChargeDate() {
        return Optional.ofNullable(chargeDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                externalId.equals(payment.externalId) &&
                amount.equals(payment.amount) &&
                state == payment.state &&
                Objects.equals(stateDetails, payment.stateDetails) &&
                Objects.equals(stateDetailsDescription, payment.stateDetailsDescription) &&
                Objects.equals(description, payment.description) &&
                Objects.equals(reference, payment.reference) &&
                Objects.equals(providerId, payment.providerId) &&
                createdDate.equals(payment.createdDate) &&
                mandate.equals(payment.mandate) &&
                Objects.equals(chargeDate, payment.chargeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalId, amount, state, stateDetails, stateDetailsDescription, description, reference, providerId, createdDate, mandate,
                chargeDate);
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", amount=" + amount +
                ", state=" + state +
                ", stateDetails='" + stateDetails + '\'' +
                ", stateDetailsDescription='" + stateDetailsDescription + '\'' +
                ", reference='" + reference + '\'' +
                ", providerId=" + providerId +
                ", createdDate=" + createdDate +
                ", mandate=" + mandate +
                ", chargeDate=" + chargeDate +
                '}';
    }

    public static final class PaymentBuilder {
        private Long id;
        private String externalId;
        private Long amount;
        private PaymentState state;
        private String stateDetails;
        private String stateDetailsDescription;
        private String description;
        private String reference;
        private PaymentProviderPaymentId providerId;
        private ZonedDateTime createdDate;
        private Mandate mandate;
        private LocalDate chargeDate;

        private PaymentBuilder() {
        }

        public static PaymentBuilder aPayment() {
            return new PaymentBuilder();
        }

        public static PaymentBuilder fromPayment(Payment payment) {
            var builder = aPayment()
                    .withAmount(payment.getAmount())
                    .withCreatedDate(payment.getCreatedDate())
                    .withDescription(payment.getDescription())
                    .withExternalId(payment.getExternalId())
                    .withId(payment.getId())
                    .withMandate(payment.getMandate())
                    .withReference(payment.getReference())
                    .withState(payment.getState());

            payment.getChargeDate().ifPresent(builder::withChargeDate);
            payment.getProviderId().ifPresent(builder::withProviderId);

            return builder;
        }

        public PaymentBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public PaymentBuilder withAmount(Long amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder withState(PaymentState state) {
            this.state = state;
            return this;
        }

        public PaymentBuilder withStateDetails(String stateDetails) {
            this.stateDetails = stateDetails;
            return this;
        }

        public PaymentBuilder withStateDetailsDescription(String stateDetailsDescription) {
            this.stateDetailsDescription = stateDetailsDescription;
            return this;
        }

        public PaymentBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public PaymentBuilder withReference(String reference) {
            this.reference = reference;
            return this;
        }

        public PaymentBuilder withProviderId(PaymentProviderPaymentId providerId) {
            this.providerId = providerId;
            return this;
        }

        public PaymentBuilder withCreatedDate(ZonedDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public PaymentBuilder withMandate(Mandate mandate) {
            this.mandate = mandate;
            return this;
        }

        public PaymentBuilder withChargeDate(LocalDate chargeDate) {
            this.chargeDate = chargeDate;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
