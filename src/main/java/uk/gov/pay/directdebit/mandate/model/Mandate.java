package uk.gov.pay.directdebit.mandate.model;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface Mandate {
    Payer getPayer();

    GatewayAccount getGatewayAccount();

    String getReturnUrl();

    ZonedDateTime getCreatedDate();

    Long getId();

    void setId(Long id);

    MandateExternalId getExternalId();

    MandateState getState();

    void setState(MandateState state);

    MandateBankStatementReference getMandateBankStatementReference();

    String getServiceReference();

    Optional<PaymentProviderMandateId> getPaymentProviderMandateId();

}
