package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.common.model.DirectDebitStateWithDetails;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.MandateLookupKey;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import javax.inject.Inject;

public class MandateUpdateService {

    private final MandateDao mandateDao;

    @Inject
    MandateUpdateService(MandateDao mandateDao) {
        this.mandateDao = mandateDao;
    }

    public int updateStateByPaymentProviderMandateId(PaymentProvider paymentProvider, MandateLookupKey mandateLookupKey,
                                                     DirectDebitStateWithDetails<MandateState> stateAndDetails) {
        if (mandateLookupKey.getClass() == GoCardlessMandateIdAndOrganisationId.class) {
            var goCardlessMandateIdAndOrganisationId = (GoCardlessMandateIdAndOrganisationId) mandateLookupKey;
            return mandateDao.updateStateByProviderIdAndOrganisationId(paymentProvider, goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId(),
                    goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(), stateAndDetails.getState(), stateAndDetails.getDetails().orElse(null),
                    stateAndDetails.getDetailsDescription().orElse(null));
        } else if (mandateLookupKey.getClass() == SandboxMandateId.class) {
            var paymentProviderMandateId = (PaymentProviderMandateId) mandateLookupKey;
            return mandateDao.updateStateByProviderId(paymentProvider, paymentProviderMandateId, stateAndDetails.getState(),
                    stateAndDetails.getDetails().orElse(null), stateAndDetails.getDetailsDescription().orElse(null));
        }
        throw new IllegalArgumentException("Unrecognised MandateLookupKey of type " + mandateLookupKey.getClass());
    }

}
