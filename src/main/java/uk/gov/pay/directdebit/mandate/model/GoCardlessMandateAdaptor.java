package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.time.ZonedDateTime;
import java.util.Optional;

public class GoCardlessMandateAdaptor implements Mandate{

    private final com.gocardless.resources.Mandate goCardlessMandate;
    private final Mandate mandate;

    public GoCardlessMandateAdaptor(com.gocardless.resources.Mandate goCardlessMandate, Mandate mandate) {
        this.goCardlessMandate = goCardlessMandate;
        this.mandate = mandate;
    }

    @Override
    public Payer getPayer() {
        return mandate.getPayer();
    }

    @Override
    public GatewayAccount getGatewayAccount() {
        return mandate.getGatewayAccount();
    }

    @Override
    public String getReturnUrl() {
        return mandate.getReturnUrl();
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return mandate.getCreatedDate();
    }

    @Override
    public Long getId() {
        return mandate.getId();
    }

    @Override
    public void setId(Long id) {
        mandate.setId(id);
    }

    @Override
    public MandateExternalId getExternalId() {
        return mandate.getExternalId();
    }

    @Override
    public MandateState getState() {
        return mandate.getState();
    }

    @Override
    public void setState(MandateState state) {
        mandate.setState(state);
    }

    @Override
    public MandateBankStatementReference getMandateBankStatementReference() {
        return MandateBankStatementReference.valueOf(goCardlessMandate.getReference());
    }

    @Override
    public String getServiceReference() {
        return mandate.getServiceReference();
    }

    @Override
    public Optional<PaymentProviderMandateId> getPaymentProviderMandateId() {
        return Optional.of(GoCardlessMandateId.valueOf(goCardlessMandate.getId()));
    }

}
