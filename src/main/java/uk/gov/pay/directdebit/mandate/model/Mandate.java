package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

public class Mandate {

    private Long id;
    private Long payerId;
    private String externalId;

    public Mandate(Long payerId) {
        this.id = id;
        this.externalId = RandomIdGenerator.newId();
        this.payerId = payerId;
    }

    public Mandate(Long id, String externalId, Long payerId) {
        this.id = id;
        this.externalId = externalId;
        this.payerId = payerId;
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
}
