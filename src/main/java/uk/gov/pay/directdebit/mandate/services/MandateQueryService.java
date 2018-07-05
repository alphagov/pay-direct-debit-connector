package uk.gov.pay.directdebit.mandate.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

public class MandateQueryService {
    private MandateDao mandateDao;

    @Inject
    public MandateQueryService(MandateDao mandateDao) {
        this.mandateDao = mandateDao;
    }

    public Mandate findByExternalId(MandateExternalId externalId) {
        return mandateDao
                .findByExternalId(externalId)
                .orElseThrow(() -> new MandateNotFoundException(externalId));
    }

    public Mandate findById(Long mandateId) {
        return mandateDao
                .findById(mandateId)
                .orElseThrow(() -> new MandateNotFoundException(mandateId.toString()));
    }

    public List<Mandate> findAllMandatesBySetOfStatesAndMaxCreationTime(Set<MandateState> states, ZonedDateTime cutOffTime) {
        return mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
}
