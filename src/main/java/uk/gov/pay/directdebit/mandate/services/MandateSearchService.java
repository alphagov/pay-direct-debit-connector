package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.dao.MandateSearchDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class MandateSearchService {

    private MandateSearchDao mandateSearchDao;

    @Inject
    public MandateSearchService(MandateSearchDao mandateSearchDao) {
        this.mandateSearchDao = mandateSearchDao;
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
