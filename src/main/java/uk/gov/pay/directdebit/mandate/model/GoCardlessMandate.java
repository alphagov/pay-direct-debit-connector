package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorId;

public class GoCardlessMandate {

    private Long id;
    private final Long mandateId;
    private final GoCardlessMandateId goCardlessMandateId;
    private final MandateBankStatementReference goCardlessReference;
    private final GoCardlessCreditorId goCardlessCreditorId;

    public GoCardlessMandate(Long id, Long mandateId, GoCardlessMandateId goCardlessMandateId, MandateBankStatementReference goCardlessReference,
                             GoCardlessCreditorId goCardlessCreditorId) {
        this.id = id;
        this.mandateId = mandateId;
        this.goCardlessMandateId = goCardlessMandateId;
        this.goCardlessReference = goCardlessReference;
        this.goCardlessCreditorId = goCardlessCreditorId;
    }

    public GoCardlessMandate(Long mandateId, GoCardlessMandateId goCardlessMandateId, MandateBankStatementReference goCardlessReference,
                             GoCardlessCreditorId goCardlessCreditorId) {
        this(null, mandateId, goCardlessMandateId, goCardlessReference, goCardlessCreditorId);
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

    public GoCardlessMandateId getGoCardlessMandateId() {
        return goCardlessMandateId;
    }

    public MandateBankStatementReference getGoCardlessReference() {
        return goCardlessReference;
    }

    public GoCardlessCreditorId getGoCardlessCreditorId() {
        return goCardlessCreditorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessMandate that = (GoCardlessMandate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!mandateId.equals(that.mandateId)) return false;
        if (!goCardlessReference.equals(that.goCardlessReference)) return false;
        if (!goCardlessCreditorId.equals(that.goCardlessCreditorId)) return false;
        return goCardlessMandateId.equals(that.goCardlessMandateId);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + mandateId.hashCode();
        result = 31 * result + goCardlessReference.hashCode();
        result = 31 * result + goCardlessMandateId.hashCode();
        result = 31 * result + goCardlessCreditorId.hashCode();
        return result;
    }

}
