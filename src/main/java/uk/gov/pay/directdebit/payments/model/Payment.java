package uk.gov.pay.directdebit.payments.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.Mandate;

public class Payment {

    private Long id;
    private String externalId;
    private Long amount;
    private PaymentState state;
    private String description;
    private String reference;
    private PaymentProviderPaymentId providerId;
    private ZonedDateTime createdDate;
    private Mandate mandate;
    private LocalDate chargeDate;

    public Payment(Long id, String externalId, Long amount, PaymentState state, String description, String reference,
                   Mandate mandate, ZonedDateTime createdDate, PaymentProviderPaymentId paymentProviderPaymentId,
                   LocalDate chargeDate) {
        this.id = id;
        this.externalId = externalId;
        this.amount = amount;
        this.state = state;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        this.mandate = mandate;
        this.providerId = paymentProviderPaymentId;
        this.chargeDate = chargeDate;
    }

    public Payment(Long amount, PaymentState state, String description, String reference, Mandate mandate, ZonedDateTime createdDate) {
        this(null, RandomIdGenerator.newId(), amount, state, description, reference, mandate, createdDate, null, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Mandate getMandate() {
        return mandate;
    }

    public void setMandate(Mandate mandate) {
        this.mandate = mandate;
    }

    public PaymentProviderPaymentId getProviderId() {
        return providerId;
    }

    public Optional<LocalDate> getChargeDate() {
        return Optional.ofNullable(chargeDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                externalId.equals(payment.externalId) &&
                amount.equals(payment.amount) &&
                state == payment.state &&
                Objects.equals(description, payment.description) &&
                Objects.equals(reference, payment.reference) &&
                Objects.equals(providerId, payment.providerId) &&
                createdDate.equals(payment.createdDate) &&
                mandate.equals(payment.mandate) &&
                Objects.equals(chargeDate, payment.chargeDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalId, amount, state, description, reference, providerId, createdDate, mandate, chargeDate);
    }
}
