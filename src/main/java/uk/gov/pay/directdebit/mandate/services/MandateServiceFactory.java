package uk.gov.pay.directdebit.mandate.services;

import javax.inject.Inject;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payments.exception.InvalidMandateTypeException;

public class MandateServiceFactory {

    private final OnDemandMandateService onDemandMandateService;
    private final OneOffMandateService oneOffMandateService;
    private final MandateQueryService mandateQueryService;
    private final MandateService mandateService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public MandateServiceFactory(
            OnDemandMandateService onDemandMandateService,
            OneOffMandateService oneOffMandateService,
            MandateQueryService mandateQueryService,
            MandateService mandateService,
            MandateStateUpdateService mandateStateUpdateService) {
        this.onDemandMandateService = onDemandMandateService;
        this.oneOffMandateService = oneOffMandateService;
        this.mandateQueryService = mandateQueryService;
        this.mandateService = mandateService;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }

    public OnDemandMandateService getOnDemandMandateService() {
        return onDemandMandateService;
    }

    public OneOffMandateService getOneOffMandateService() {
        return oneOffMandateService;
    }

    public MandateCommandService getMandateControlService(MandateType mandateType) {
        switch(mandateType) {
            case ONE_OFF:
                return oneOffMandateService;
            case ON_DEMAND:
                return onDemandMandateService;
        }
        
        throw new InvalidMandateTypeException(mandateType);
    }

    public MandateQueryService getMandateQueryService() {
        return mandateQueryService;
    }

    public MandateService getMandateService() {
        return mandateService;
    }

    public MandateStateUpdateService getMandateStateUpdateService() {
        return mandateStateUpdateService;
    }
}
