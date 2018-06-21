package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.time.ZonedDateTime;

public class Mandate {
    private Long id;
    private final String externalId;
    private MandateState state;
    private final GatewayAccount gatewayAccount;
    private final String returnUrl;
    private final MandateType type;
    private final String mandateReference;
    private final ZonedDateTime createdDate;
    private Payer payer;

    public Mandate(Long id, GatewayAccount gatewayAccount, MandateType type, String externalId,
                   String mandateReference,
                   MandateState state, String returnUrl, ZonedDateTime createdDate,
                   Payer payer) {
        this.id = id;
        this.gatewayAccount = gatewayAccount;
        this.type = type;
        this.externalId = externalId;
        this.mandateReference = mandateReference;
        this.state = state;
        this.returnUrl = returnUrl;
        this.createdDate = createdDate;
        this.payer = payer;
    }

    public Mandate(GatewayAccount gatewayAccount, MandateType type,
                   MandateState state, String returnUrl, ZonedDateTime createdDate,
                   Payer payer) {
        this(null, gatewayAccount, type, RandomIdGenerator.newId(), "default-ref", state, returnUrl,
                createdDate, payer);
    }

    public Payer getPayer() {
        return payer;
    }

    public GatewayAccount getGatewayAccount() {
        return gatewayAccount;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
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

    public MandateState getState() {
        return state;
    }

    public void setState(MandateState state) {
        this.state = state;
    }

    public String getMandateReference() {
        return mandateReference;
    }

    public MandateType getType() {
        return type;
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
        if (mandateReference != null ? !mandateReference.equals(mandate.mandateReference) : mandate.mandateReference != null) {
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
        result = 31 * result + (mandateReference != null ? mandateReference.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        return result;
    }
}
