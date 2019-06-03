package uk.gov.pay.directdebit.mandate.services;

import javax.inject.Inject;

//TODO do we need this factory?
public class MandateServiceFactory {

    private final OnDemandMandateService onDemandMandateService;
    private final MandateQueryService mandateQueryService;
    private final MandateService mandateService;
    private final MandateStateUpdateService mandateStateUpdateService;

    @Inject
    public MandateServiceFactory(OnDemandMandateService onDemandMandateService,
                                 MandateQueryService mandateQueryService,
                                 MandateService mandateService,
                                 MandateStateUpdateService mandateStateUpdateService) {
        this.onDemandMandateService = onDemandMandateService;
        this.mandateQueryService = mandateQueryService;
        this.mandateService = mandateService;
        this.mandateStateUpdateService = mandateStateUpdateService;
    }

    public OnDemandMandateService getOnDemandMandateService() {
        return onDemandMandateService;
    }

    public MandateCommandService getMandateCommandService() {
        return onDemandMandateService;
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
