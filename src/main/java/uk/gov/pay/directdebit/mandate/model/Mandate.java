package uk.gov.pay.directdebit.mandate.model;

import java.time.ZonedDateTime;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.model.Payer;

public class Mandate {
    private Long id;
    private String externalId;
    private MandateState state;
    private GatewayAccount gatewayAccount;
    private String returnUrl;
    private MandateType type;
    private String reference;
    private ZonedDateTime createdDate;
    private Payer payer;
    
    public Mandate(Long id, GatewayAccount gatewayAccount, MandateType type, String externalId,
            String reference,
            MandateState state, String returnUrl, ZonedDateTime createdDate,
            Payer payer) {
        this.id = id;
        this.gatewayAccount = gatewayAccount;
        this.type = type;
        this.externalId = externalId;
        this.reference = reference;
        this.state = state;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.payer = payer;
    }

    public Payer getPayer() {
        return payer;
    }

    public Mandate setPayer(Payer payer) {
        this.payer = payer;
        return this;
    }

    public Mandate(GatewayAccount gatewayAccount, MandateType type, 
            MandateState state, String returnUrl, ZonedDateTime createdDate,
            Payer payer) {
        this(null, gatewayAccount, type, RandomIdGenerator.newId(), "default-ref", state, returnUrl,
                createdDate, payer);
    }

    public GatewayAccount getGatewayAccount() {
        return gatewayAccount;
    }

    public void setGatewayAccount(GatewayAccount gatewayAccount) {
        this.gatewayAccount = gatewayAccount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public MandateState getState() {
        return state;
    }

    public void setState(MandateState state) {
        this.state = state;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public MandateType getType() {
        return type;
    }

    public void setType(MandateType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Mandate mandate = (Mandate) o;

        if (id != null ? !id.equals(mandate.id) : mandate.id != null) {
            return false;
        }
        if (!externalId.equals(mandate.externalId)) {
            return false;
        }
        if (state != mandate.state) {
            return false;
        }
        if (!gatewayAccount.equals(mandate.gatewayAccount)) {
            return false;
        }
        if (!returnUrl.equals(mandate.returnUrl)) {
            return false;
        }
        if (type != mandate.type) {
            return false;
        }
        if (reference != null ? !reference.equals(mandate.reference) : mandate.reference != null) {
            return false;
        }
        if (payer != null ? !payer.equals(mandate.payer) : mandate.payer != null) {
            return false;
        }
        return createdDate.equals(mandate.createdDate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + externalId.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (payer != null ? payer.hashCode() : 0);
        result = 31 * result + gatewayAccount.hashCode();
        result = 31 * result + returnUrl.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (reference != null ? reference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        return result;
    }
}
