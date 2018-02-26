package uk.gov.pay.directdebit.mandate.model;

import com.google.common.base.Objects;

public class GoCardlessMandate {

    private Long id;
    private Long mandateId;
    private String goCardlessMandateId;


    public GoCardlessMandate(Long id, Long mandateId, String goCardlessMandateId) {
        this.id = id;
        this.mandateId = mandateId;
        this.goCardlessMandateId = goCardlessMandateId;
    }
    public GoCardlessMandate(Long mandateId, String goCardlessMandateId) {
        this(null, mandateId, goCardlessMandateId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public void setMandateId(Long mandateId) {
        this.mandateId = mandateId;
    }

    public String getGoCardlessMandateId() {
        return goCardlessMandateId;
    }

    public void setGoCardlessMandateId(String goCardlessMandateId) {
        this.goCardlessMandateId = goCardlessMandateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoCardlessMandate that = (GoCardlessMandate) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(mandateId, that.mandateId) &&
                Objects.equal(goCardlessMandateId, that.goCardlessMandateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, mandateId, goCardlessMandateId);
    }
}
