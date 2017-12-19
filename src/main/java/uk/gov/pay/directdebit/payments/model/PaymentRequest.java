package uk.gov.pay.directdebit.payments.model;


import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PaymentRequest {

    private Long id;
    private String externalId;
    private Long amount;
    private String returnUrl;
    private Long gatewayAccountId;
    private String description;
    private String reference;
    private ZonedDateTime createdDate;

    public PaymentRequest(Long id, Long amount, String returnUrl, Long gatewayAccountId, String description, String reference, String externalId, ZonedDateTime createdDate) {
        this.id = id;
        this.amount = amount;
        this.returnUrl = returnUrl;
        this.gatewayAccountId = gatewayAccountId;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        this.externalId = externalId;
    }

    public PaymentRequest(Long amount, String returnUrl, Long gatewayAccountId, String description, String reference) {
        this(null, amount, returnUrl, gatewayAccountId, description, reference, RandomIdGenerator.newId(), ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public Long getGatewayAccountId() {
        return gatewayAccountId;
    }

    public void setGatewayAccountId(Long gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentRequest that = (PaymentRequest) o;

        if (!id.equals(that.id)) return false;
        if (!externalId.equals(that.externalId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!returnUrl.equals(that.returnUrl)) return false;
        if (!gatewayAccountId.equals(that.gatewayAccountId)) return false;
        if (!description.equals(that.description)) return false;
        if (!reference.equals(that.reference)) return false;
        return createdDate.equals(that.createdDate);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + externalId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + gatewayAccountId.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + reference.hashCode();
        result = 31 * result + createdDate.hashCode();
        return result;
    }
}
