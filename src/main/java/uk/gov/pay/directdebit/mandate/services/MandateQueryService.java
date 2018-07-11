package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import javax.inject.Inject;

public class MandateQueryService {
    private MandateDao mandateDao;

    @Inject
    public MandateQueryService(MandateDao mandateDao) {
        this.mandateDao = mandateDao;
    }

    public Mandate findByExternalId(String externalId) {
        return mandateDao
                .findByExternalId(externalId)
                .orElseThrow(() -> new MandateNotFoundException(externalId));
    }
}
