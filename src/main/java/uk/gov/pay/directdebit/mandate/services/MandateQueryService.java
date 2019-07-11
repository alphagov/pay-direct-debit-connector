package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.dao.MandateSearchDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateLookupKey;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;
import uk.gov.pay.directdebit.payments.model.GoCardlessMandateIdAndOrganisationId;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MandateQueryService {
    private MandateDao mandateDao;
    private MandateSearchDao mandateSearchDao;

    @Inject
    public MandateQueryService(MandateDao mandateDao, MandateSearchDao mandateSearchDao) {
        this.mandateDao = mandateDao;
        this.mandateSearchDao = mandateSearchDao;
    }

    public Mandate findByExternalId(MandateExternalId externalId) {
        return mandateDao
                .findByExternalId(externalId)
                .orElseThrow(() -> new MandateNotFoundException(externalId));
    }

    public Mandate findByExternalIdAndGatewayAccountExternalId(MandateExternalId mandateExternalId, String gatewayAccountExternalId) {
        return mandateDao
                .findByExternalIdAndGatewayAccountExternalId(mandateExternalId, gatewayAccountExternalId)
                .orElseThrow(() -> new MandateNotFoundException(mandateExternalId, gatewayAccountExternalId));
    }

    public Mandate findByPaymentProviderMandateId(PaymentProvider paymentProvider, MandateLookupKey mandateLookupKey) {
        if (mandateLookupKey.getClass() == GoCardlessMandateIdAndOrganisationId.class) {
            var goCardlessMandateIdAndOrganisationId = (GoCardlessMandateIdAndOrganisationId) mandateLookupKey;
            return mandateDao
                    .findByPaymentProviderMandateIdAndOrganisation(paymentProvider, goCardlessMandateIdAndOrganisationId.getGoCardlessMandateId(),
                            goCardlessMandateIdAndOrganisationId.getGoCardlessOrganisationId())
                    .orElseThrow(() -> new MandateNotFoundException(goCardlessMandateIdAndOrganisationId));
        } else if (mandateLookupKey.getClass() == SandboxMandateId.class) {
            var paymentProviderMandateId = (PaymentProviderMandateId) mandateLookupKey;
            return mandateDao
                    .findByPaymentProviderMandateId(paymentProvider, paymentProviderMandateId)
                    .orElseThrow(() -> new MandateNotFoundException(paymentProviderMandateId.toString()));
        }
        throw new IllegalArgumentException("Unrecognised MandateLookupKey of type " + mandateLookupKey.getClass());
    }

    public Mandate findById(Long mandateId) {
        return mandateDao
                .findById(mandateId)
                .orElseThrow(() -> new MandateNotFoundException(mandateId.toString()));
    }

    public List<Mandate> findAllMandatesBySetOfStatesAndMaxCreationTime(Set<MandateState> states, ZonedDateTime cutOffTime) {
        return mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
    
    public MandateSearchResults search(MandateSearchParams params, String gatewayAccountExternalId) {
        int totalMatchingMandates = mandateSearchDao.countTotalMatchingMandates(params, gatewayAccountExternalId);
        List<Mandate> mandatesForRequestedPage = 
                totalMatchingMandates > 0 ? mandateSearchDao.search(params, gatewayAccountExternalId) : Collections.emptyList();

        return new MandateSearchResults(totalMatchingMandates, mandatesForRequestedPage);
    }
    
    public static class MandateSearchResults {
        private final int totalMatchingMandates;
        private final List<Mandate> mandatesForRequestedPage;

        MandateSearchResults(int totalMatchingMandates, List<Mandate> mandatesForRequestedPage) {
            this.totalMatchingMandates = totalMatchingMandates;
            this.mandatesForRequestedPage = mandatesForRequestedPage;
        }

        public int getTotalMatchingMandates() {
            return totalMatchingMandates;
        }

        public List<Mandate> getMandatesForRequestedPage() {
            return mandatesForRequestedPage;
        }
    }
}
