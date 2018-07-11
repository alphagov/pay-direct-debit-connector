package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateConfirmationRequest;

public interface MandateCommandService {
    void confirm(GatewayAccount gatewayAccount, Mandate mandate, MandateConfirmationRequest confirmDetailsRequest);
}
