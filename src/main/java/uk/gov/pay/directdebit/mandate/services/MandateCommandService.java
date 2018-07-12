package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.model.Mandate;

public interface MandateCommandService {
    void confirm(GatewayAccount gatewayAccount, Mandate mandate, ConfirmMandateRequest confirmDetailsRequest);
}
