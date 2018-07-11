package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.mandate.model.Mandate;

import javax.inject.Inject;

public class MandateServiceFactory {

    private final OnDemandMandateService onDemandMandateService;
    private final OneOffMandateService oneOffMandateService;
    private final MandateQueryService mandateQueryService;

    @Inject
    public MandateServiceFactory(
            OnDemandMandateService onDemandMandateService,
            OneOffMandateService oneOffMandateService, 
            MandateQueryService mandateQueryService) {
        this.onDemandMandateService = onDemandMandateService;
        this.oneOffMandateService = oneOffMandateService;
        this.mandateQueryService = mandateQueryService;
    }

    public MandateCommandService getMandateControlService(Mandate mandate) {
        switch(mandate.getType()) {
            case ONE_OFF:
                return oneOffMandateService;
            case ON_DEMAND:
                return onDemandMandateService;
        }
        
        throw new RuntimeException();
    }

    public MandateQueryService getMandateQueryService() {
        return mandateQueryService;
    }
}
