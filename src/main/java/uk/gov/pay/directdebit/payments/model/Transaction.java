package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Transaction {

    private Long id;
    private PaymentRequest paymentRequest;
    private Long gatewayAccountId;
    private String gatewayAccountExternalId;
    private PaymentProvider paymentProvider;
    private Long amount;
    private Type type;
    private PaymentState state;

    public enum Type {
        CHARGE
    }

    public Transaction(Long id,
                       Long paymentRequestId,
                       String paymentRequestExternalId,
                       String paymentRequestDescription,
                       String paymentRequestReference,
                       Long gatewayAccountId,
                       String gatewayAccountExternalId,
                       PaymentProvider paymentProvider,
                       String paymentRequestReturnUrl,
                       Long amount,
                       Type type,
                       PaymentState state) {
        this.id = id;
        this.paymentRequest = new PaymentRequest(
                paymentRequestId,
                amount,
                paymentRequestReturnUrl,
                gatewayAccountId,
                paymentRequestDescription,
                paymentRequestReference,
                paymentRequestExternalId,
                ZonedDateTime.now(ZoneOffset.UTC)
        );
        this.gatewayAccountId = gatewayAccountId;
        this.gatewayAccountExternalId = gatewayAccountExternalId;
        this.paymentProvider = paymentProvider;
        this.amount = amount;
        this.type = type;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentProvider(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getPaymentRequestReturnUrl() {
        return this.paymentRequest.getReturnUrl();
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public String getGatewayAccountExternalId() {
        return gatewayAccountExternalId;
    }

    public String getPaymentRequestDescription() {
        return this.paymentRequest.getDescription();
    }

    public String getPaymentRequestReference() {
        return this.paymentRequest.getReference();
    }

    public PaymentProvider getPaymentProvider() {
        return paymentProvider;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!paymentRequest.equals(that.paymentRequest)) return false;
        if (!gatewayAccountId.equals(that.gatewayAccountId)) return false;
        if (!gatewayAccountExternalId.equals(that.gatewayAccountExternalId)) return false;
        if (paymentProvider != that.paymentProvider) return false;
        if (!amount.equals(that.amount)) return false;
        if (type != that.type) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + paymentRequest.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + gatewayAccountExternalId.hashCode();
        result = 31 * result + paymentProvider.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

}
