package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Transaction {

    private Long id;
    private String paymentRequestExternalId;
    private Long paymentRequestId;
    private String paymentRequestReturnUrl;
    private Long amount;
    private Type type;
    private PaymentState state;

    public Transaction(Long id, Long paymentRequestId, String paymentRequestExternalId, String paymentRequestReturnUrl, Long amount, Type type, PaymentState state) {
        this.id = id;
        this.paymentRequestExternalId = paymentRequestExternalId;
        this.paymentRequestId = paymentRequestId;
        this.paymentRequestReturnUrl = paymentRequestReturnUrl;
        this.amount = amount;
        this.type = type;
        this.state = state;
    }

    public Transaction(Long paymentRequestId, String paymentRequestExternalId, String paymentRequestReturnUrl, Long amount, Type type, PaymentState state) {
        this(null, paymentRequestId, paymentRequestExternalId, paymentRequestReturnUrl, amount, type, state);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }

    public String getPaymentRequestExternalId() {
        return paymentRequestExternalId;
    }

    public void setPaymentRequestExternalId(String paymentRequestExternalId) {
        this.paymentRequestExternalId = paymentRequestExternalId;
    }

    public String getPaymentRequestReturnUrl() {
        return paymentRequestReturnUrl;
    }

    public void setPaymentRequestReturnUrl(String paymentRequestReturnUrl) {
        this.paymentRequestReturnUrl = paymentRequestReturnUrl;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }
    public enum Type {
        CHARGE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!paymentRequestExternalId.equals(that.paymentRequestExternalId)) return false;
        if (!paymentRequestId.equals(that.paymentRequestId)) return false;
        if (!paymentRequestReturnUrl.equals(that.paymentRequestReturnUrl)) return false;
        if (!amount.equals(that.amount)) return false;
        if (type != that.type) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + paymentRequestExternalId.hashCode();
        result = 31 * result + paymentRequestId.hashCode();
        result = 31 * result + paymentRequestReturnUrl.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }
}
