package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

public class Mandate {

    private Long id;
    private Long payerId;
    private String externalId;
    private MandateState state;
    private String reference;

    public Mandate(Long id, String externalId, Long payerId, String reference, MandateState state) {
        this.id = id;
        this.externalId = externalId;
        this.payerId = payerId;
        this.reference = reference;
        this.state = state;
    }

    public Mandate(Long payerId, String reference) {
        this(null, RandomIdGenerator.newId(), payerId, reference, MandateState.PENDING);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPayerId() {
        return payerId;
    }

    public void setPayerId(Long payerId) {
        this.payerId = payerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public MandateState getState() {
        return state;
    }

    public void setState(MandateState state) {
        this.state = state;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mandate)) return false;

        Mandate mandate = (Mandate) o;

        if (!id.equals(mandate.id)) return false;
        if (!payerId.equals(mandate.payerId)) return false;
        return externalId.equals(mandate.externalId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + payerId.hashCode();
        result = 31 * result + externalId.hashCode();
        return result;
    }
}
