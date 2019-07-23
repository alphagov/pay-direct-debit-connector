package uk.gov.pay.directdebit.mandate.services;

import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;


public class MandateStateUpdateService {

    private final MandateDao mandateDao;
    private final UserNotificationService userNotificationService;
    private final GovUkPayEventService govUkPayEventService;

    @Inject
    public MandateStateUpdateService(
            MandateDao mandateDao,
            UserNotificationService userNotificationService,
            GovUkPayEventService govUkPayEventService) {
        this.mandateDao = mandateDao;
        this.userNotificationService = userNotificationService;
        this.govUkPayEventService = govUkPayEventService;
    }
    
    Mandate mandateCreatedFor(Mandate mandate) {
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_CREATED);
    }

    Mandate confirmedDirectDebitDetailsFor(Mandate mandate) {
        confirmedDetailsFor(mandate);
        userNotificationService.sendMandateCreatedEmailFor(mandate);
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_SUBMITTED);
    }

    Mandate changePaymentMethodFor(Mandate mandate) {
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    Mandate cancelMandateCreation(Mandate mandate) {
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_CANCELLED_BY_USER);
    }

    public Mandate mandateExpiredFor(Mandate mandate) {
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_EXPIRED_BY_SYSTEM);
    }

    Mandate tokenExchangedFor(Mandate mandate) {
        return govUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_TOKEN_EXCHANGED);
    }

    private void confirmedDetailsFor(Mandate mandate) {
        mandateDao.updateReferenceAndPaymentProviderId(mandate);
    }

    public void mandateFailedFor(Mandate mandate) {
        userNotificationService.sendMandateFailedEmailFor(mandate);
    }

    public void mandateCancelledFor(Mandate mandate) {
        userNotificationService.sendMandateCancelledEmailFor(mandate);
    }
}
