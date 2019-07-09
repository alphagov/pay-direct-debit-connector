package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderServiceId;
import uk.gov.pay.directdebit.mandate.api.MandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.dao.MandateSearchDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.params.MandateSearchParams;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    public Mandate findByPaymentProviderMandateId(PaymentProvider paymentProvider, PaymentProviderMandateId paymentProviderMandateId,
                                                  PaymentProviderServiceId paymentProviderServiceId) {
        return mandateDao
                .findByPaymentProviderMandateId(paymentProvider, paymentProviderMandateId, paymentProviderServiceId)
                .orElseThrow(() -> new MandateNotFoundException(paymentProviderMandateId.toString()));
    }

    public Mandate findById(Long mandateId) {
        return mandateDao
                .findById(mandateId)
                .orElseThrow(() -> new MandateNotFoundException(mandateId.toString()));
    }

    public List<Mandate> findAllMandatesBySetOfStatesAndMaxCreationTime(Set<MandateState> states, ZonedDateTime cutOffTime) {
        return mandateDao.findAllMandatesBySetOfStatesAndMaxCreationTime(states, cutOffTime);
    }
    
    public List<MandateResponse> findAllMandatesThatMatchSearchParams(MandateSearchParams mandateSearchParams, String gatewayAccountId) {
        return mandateSearchDao
                .search(mandateSearchParams, gatewayAccountId)
                .stream()
                .map(this::convertMandateToMandateResponse)
                .collect(Collectors.toList());
    }
    
    private MandateResponse convertMandateToMandateResponse(Mandate mandate) {
        return new MandateResponse(mandate, null);
    }
}
