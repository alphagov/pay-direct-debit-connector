package uk.gov.pay.directdebit.mandate.model;

public final class GoCardlessMandate {

    private Long id;
    private final Long mandateId;
    private final String goCardlessMandateId;
    private final String goCardlessReference;

    public GoCardlessMandate(Long id, Long mandateId, String goCardlessMandateId, String goCardlessReference) {
        this.id = id;
        this.mandateId = mandateId;
        this.goCardlessMandateId = goCardlessMandateId;
        this.goCardlessReference = goCardlessReference;
    }
    public GoCardlessMandate(Long mandateId, String goCardlessMandateId, String goCardlessReference) {
        this(null, mandateId, goCardlessMandateId, goCardlessReference);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessMandate that = (GoCardlessMandate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!mandateId.equals(that.mandateId)) return false;
        if (!goCardlessReference.equals(that.goCardlessReference)) return false;
        return goCardlessMandateId.equals(that.goCardlessMandateId);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + mandateId.hashCode();
        result = 31 * result + goCardlessReference.hashCode();
        result = 31 * result + goCardlessMandateId.hashCode();
        return result;
    }
}
