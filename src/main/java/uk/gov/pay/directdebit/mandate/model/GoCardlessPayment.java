package uk.gov.pay.directdebit.mandate.model;

public class GoCardlessPayment {

    private Long id;
    private Long transactionId;
    private String paymentId;

    public GoCardlessPayment(Long id, Long transactionId, String paymentId) {
        this.id = id;
        this.transactionId = transactionId;
        this.paymentId = paymentId;
    }

    public GoCardlessPayment(Long transactionId, String paymentId) {
        this(null, transactionId, paymentId);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessPayment that = (GoCardlessPayment) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!transactionId.equals(that.transactionId)) return false;
        return paymentId.equals(that.paymentId);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + transactionId.hashCode();
        result = 31 * result + paymentId.hashCode();
        return result;
    }
}
