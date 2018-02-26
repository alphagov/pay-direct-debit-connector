package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

public class Mandate {

    private Long id;
    private Long payerId;
    private String externalId;

    public Mandate(Long id, String externalId, Long payerId) {
        this.id = id;
        this.externalId = externalId;
        this.payerId = payerId;
    }

    public Mandate(Long payerId) {
        this(null,  RandomIdGenerator.newId(), payerId);
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
