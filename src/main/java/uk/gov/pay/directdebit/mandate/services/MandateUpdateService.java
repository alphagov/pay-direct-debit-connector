package uk.gov.pay.directdebit.mandate.services;

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

    public int updateStateByPaymentProviderMandateId(PaymentProvider paymentProvider, MandateLookupKey mandateLookupKey, MandateState mandateState) {
        if (mandateLookupKey.getClass() == GoCardlessMandateIdAndOrganisationId.class) {
            var goCardlessMandateIdAndOrganisationId = (GoCardlessMandateIdAndOrganisationId) mandateLookupKey;
            return mandateDao.updateStateByPaymentProviderMandateIdAndOrganisationId(paymentProvider, goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId(),
                    goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(), mandateState);
        } else if (mandateLookupKey.getClass() == SandboxMandateId.class) {
            var paymentProviderMandateId = (PaymentProviderMandateId) mandateLookupKey;
            return mandateDao.updateStateByPaymentProviderMandateIdAndOrganisationId(paymentProvider, paymentProviderMandateId, mandateState);
        }
        throw new IllegalArgumentException("Unrecognised MandateLookupKey of type " + mandateLookupKey.getClass());
    }

}
