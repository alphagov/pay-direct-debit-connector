package uk.gov.pay.directdebit.payments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Transaction {

    private Long id;

    private PaymentRequest paymentRequest;

    private GatewayAccount gatewayAccount;

    private Type type;

    private PaymentState state;

    public Transaction(Long id,
                       PaymentRequest paymentRequest,
                       GatewayAccount gatewayAccount,
                       Type type,
                       PaymentState state) {
        this.id = id;
        this.paymentRequest = paymentRequest;
        this.gatewayAccount = gatewayAccount;
        this.type = type;
        this.state = state;
    }

    public Transaction(PaymentRequest paymentRequest,
                       GatewayAccount gatewayAccount,
                       Type type,
                       PaymentState state) {
        this(null, paymentRequest, gatewayAccount, type, state);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentRequestId() {
        return paymentRequest.getId();
    }

    public String getPaymentRequestExternalId() {
        return paymentRequest.getExternalId();
    }

    public String getPaymentRequestReturnUrl() {
        return paymentRequest.getReturnUrl();
    }

    public Long getGatewayAccountId() {
        return gatewayAccount.getId();
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccount.getExternalId();
    }

    public String getPaymentRequestDescription() {
        return paymentRequest.getDescription();
    }

    public String getPaymentRequestReference() {
        return paymentRequest.getReference();
    }

    public PaymentProvider getPaymentProvider() {
        return gatewayAccount.getPaymentProvider();
    }

    public Long getAmount() {
        return paymentRequest.getAmount();
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
        if (!paymentRequest.equals(that.paymentRequest)) return false;
        if (!gatewayAccount.equals(that.gatewayAccount)) return false;
        if (type != that.type) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + paymentRequest.hashCode();
        result = 31 * result + gatewayAccount.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

}
