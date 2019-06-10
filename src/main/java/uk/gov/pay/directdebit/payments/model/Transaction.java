package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import java.time.ZonedDateTime;

public class Transaction {

    private Long id;
    private String externalId;
    private Long amount;
    private PaymentState state;
    private String description;
    private String reference;
    private ZonedDateTime createdDate;
    private Mandate mandate;

    public Transaction(Long id, String externalId, Long amount, PaymentState state, String description, String reference, Mandate mandate, ZonedDateTime createdDate) {
        this.id = id;
        this.externalId = externalId;
        this.amount = amount;
        this.state = state;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        this.mandate = mandate;
    }

    public Transaction(Long amount, PaymentState state, String description, String reference, Mandate mandate, ZonedDateTime createdDate) {
        this(null, RandomIdGenerator.newId(), amount, state, description, reference, mandate, createdDate);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Transaction that = (Transaction) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (!externalId.equals(that.externalId)) {
            return false;
        }
        if (!amount.equals(that.amount)) {
            return false;
        }
        if (state != that.state) {
            return false;
        }
        if (description != null ? !description.equals(that.description)
                : that.description != null) {
            return false;
        }
        if (reference != null ? !reference.equals(that.reference) : that.reference != null) {
            return false;
        }
        if (!createdDate.equals(that.createdDate)) {
            return false;
        }
        return mandate.equals(that.mandate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + externalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        result = 31 * result + mandate.hashCode();
        return result;
    }
}
