package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Transaction {

    private Long id;
    private String paymentRequestExternalId;
    private Long paymentRequestId;
    private Long amount;
    private Type type;
    private PaymentState state;

    public Transaction(Long id, Long paymentRequestId, String paymentRequestExternalId, Long amount, Type type, PaymentState state) {
        this.id = id;
        this.paymentRequestExternalId = paymentRequestExternalId;
        this.paymentRequestId = paymentRequestId;
        this.amount = amount;
        this.type = type;
        this.state = state;
    }

    public Transaction(Long paymentRequestId, String paymentRequestExternalId, Long amount, Type type, PaymentState state) {
        this(null, paymentRequestId, paymentRequestExternalId, amount, type, state);
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
        CHARGE, REFUND
    }
}
