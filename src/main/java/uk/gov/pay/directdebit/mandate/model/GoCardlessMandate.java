package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.model.subtype.CreditorId;

public class GoCardlessMandate {

    private Long id;
    private final Long mandateId;
    private final String goCardlessMandateId;
    private final String goCardlessReference;
    private final CreditorId creditorId;

    public GoCardlessMandate(Long id, Long mandateId, String goCardlessMandateId, String goCardlessReference, CreditorId creditorId) {
        this.id = id;
        this.mandateId = mandateId;
        this.goCardlessMandateId = goCardlessMandateId;
        this.goCardlessReference = goCardlessReference;
        this.creditorId = creditorId;
    }

    public GoCardlessMandate(Long mandateId, String goCardlessMandateId, String goCardlessReference, CreditorId creditorId) {
        this(null, mandateId, goCardlessMandateId, goCardlessReference, creditorId);
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

    public String getGoCardlessMandateId() {
        return goCardlessMandateId;
    }

    public String getGoCardlessReference() {
        return goCardlessReference;
    }

    public CreditorId getCreditorId() {
        return creditorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessMandate that = (GoCardlessMandate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!mandateId.equals(that.mandateId)) return false;
        if (!goCardlessReference.equals(that.goCardlessReference)) return false;
        if (creditorId != null ? !creditorId.equals(that.creditorId) : that.creditorId != null) return false;
        return goCardlessMandateId.equals(that.goCardlessMandateId);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + mandateId.hashCode();
        result = 31 * result + goCardlessReference.hashCode();
        result = 31 * result + goCardlessMandateId.hashCode();
        result = 31 * result + (creditorId != null ? creditorId.hashCode() : 0);
        return result;
    }

}
