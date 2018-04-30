package uk.gov.pay.directdebit.payments.model;

import java.time.ZonedDateTime;

public class PaymentView {
    private String gatewayExternalId;
    private String paymentRequestId;
    private Long amount;
    private String reference;
    private String description;
    private String returnUrl;
    private ZonedDateTime createdDate;
    private String name;
    private String email;
    private PaymentState state;

    public PaymentView(String gatewayExternalId,
                       String paymentRequestId,
                       Long amount,
                       String reference,
                       String description,
                       String returnUrl,
                       ZonedDateTime createdDate,
                       String name,
                       String email,
                       PaymentState state) {
        this.gatewayExternalId = gatewayExternalId;
        this.paymentRequestId = paymentRequestId;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.name = name;
        this.email = email;
        this.state = state;
    }

    public String getGatewayExternalId() {
        return gatewayExternalId;
    }

    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public PaymentState getState() {
        return state;
    }
}
