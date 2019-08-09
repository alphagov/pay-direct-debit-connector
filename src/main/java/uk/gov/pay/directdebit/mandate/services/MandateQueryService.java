package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.common.exception.MandateIdInvalidException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;

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

    public Mandate findByExternalIdAndGatewayAccountExternalId(MandateExternalId mandateExternalId, String gatewayAccountExternalId) {
        return mandateDao
                .findByExternalIdAndGatewayAccountExternalId(mandateExternalId, gatewayAccountExternalId)
                .orElseThrow(() -> new MandateIdInvalidException(mandateExternalId, gatewayAccountExternalId));
    }

    public Mandate findByGoCardlessMandateIdAndOrganisationId(
            GoCardlessMandateId goCardlessMandateId,
            GoCardlessOrganisationId organisationId) {
        return mandateDao.findByPaymentProviderMandateIdAndOrganisation(GOCARDLESS, goCardlessMandateId, organisationId)
                .orElseThrow(() -> new MandateNotFoundException(goCardlessMandateId, organisationId));
    }
    
    public Mandate findBySandboxMandateId(SandboxMandateId sandboxMandateId) {
        return mandateDao.findByPaymentProviderMandateId(PaymentProvider.SANDBOX, sandboxMandateId)
                .orElseThrow(() -> new MandateNotFoundException(sandboxMandateId.toString()));
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
