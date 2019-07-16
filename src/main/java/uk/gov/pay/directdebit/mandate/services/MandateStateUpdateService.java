package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.events.services.DirectDebitEventService;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;

import javax.inject.Inject;

import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CANCELLED_BY_USER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_EXPIRED_BY_SYSTEM;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_SUBMITTED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEvent.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.fromMandate;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.TOKEN_EXCHANGED;


public class MandateStateUpdateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final DirectDebitEventService directDebitEventService;
    private final UserNotificationService userNotificationService;
    private final GovUkPayEventService govUkPayEventService;

    @Inject
    public MandateStateUpdateService(
            MandateDao mandateDao,
            DirectDebitEventService directDebitEventService,
            UserNotificationService userNotificationService,
            GovUkPayEventService govUkPayEventService) {
        this.directDebitEventService = directDebitEventService;
        this.mandateDao = mandateDao;
        this.userNotificationService = userNotificationService;
        this.govUkPayEventService = govUkPayEventService;
    }
    
    void mandateCreatedFor(Mandate mandate) {
        directDebitEventService.registerMandateCreatedEventFor(mandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_CREATED);
    }

    Mandate confirmedDirectDebitDetailsFor(Mandate mandate) {
        Mandate updatedMandate = confirmedDetailsFor(mandate);
        userNotificationService.sendMandateCreatedEmailFor(updatedMandate);
        directDebitEventService.registerDirectDebitConfirmedEventFor(updatedMandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_SUBMITTED);
        return updatedMandate;
    }

    void changePaymentMethodFor(Mandate mandate) {
        Mandate newMandate = updateStateFor(mandate, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        directDebitEventService.registerPaymentMethodChangedEventFor(newMandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_CANCELLED_BY_USER_NOT_ELIGIBLE);
    }

    void cancelMandateCreation(Mandate mandate) {
        Mandate newMandate = updateStateFor(mandate, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        directDebitEventService.registerMandateCancelledEventFor(newMandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_CANCELLED_BY_USER);
    }

    public DirectDebitEvent mandateActiveFor(Mandate mandate) {
        return directDebitEventService.registerMandateActiveEventFor(mandate);
    }

    public void mandateExpiredFor(Mandate mandate) {
        Mandate updatedMandate = updateStateFor(mandate, SupportedEvent.MANDATE_EXPIRED_BY_SYSTEM);
        directDebitEventService.registerMandateExpiredEventFor(updatedMandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_EXPIRED_BY_SYSTEM);
    }

    public DirectDebitEvent mandateFailedFor(Mandate mandate) {
        userNotificationService.sendMandateFailedEmailFor(mandate);
        return directDebitEventService.registerMandateFailedEventFor(mandate);
    }

    public DirectDebitEvent mandateCancelledFor(Mandate mandate) {
        userNotificationService.sendMandateCancelledEmailFor(mandate);
        return directDebitEventService.registerMandateCancelledEventFor(mandate);
    }

    public DirectDebitEvent mandatePendingFor(Mandate mandate) {
        return directDebitEventService.registerMandatePendingEventFor(mandate);
    }

    public Mandate receiveDirectDebitDetailsFor(Mandate mandate) {
        directDebitEventService.registerDirectDebitReceivedEventFor(mandate);
        return mandate;
    }

    public void payerCreatedFor(Mandate mandate) {
        directDebitEventService.registerPayerCreatedEventFor(mandate);
    }

    public void payerEditedFor(Mandate mandate) {
        directDebitEventService.registerPayerEditedEventFor(mandate);
    }

    public Mandate tokenExchangedFor(Mandate mandate) {
        Mandate newMandate = updateStateFor(mandate, TOKEN_EXCHANGED);
        directDebitEventService.registerTokenExchangedEventFor(newMandate);
        govUkPayEventService.storeEventForMandate(mandate, MANDATE_TOKEN_EXCHANGED);
        return newMandate;
    }

    private Mandate updateStateFor(Mandate mandate, DirectDebitEvent.SupportedEvent event) {
        MandateState newState = MandateStatesGraph
                .getStates()
                .getNextStateForEvent(mandate.getState(), event)
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), mandate.getState().toString()));

                mandateDao.updateState(mandate.getId(), newState);
        LOGGER.info("Updating mandate {} - from {} to {}",
                mandate.getExternalId(),
                mandate.getState(),
                newState);

        return fromMandate(mandate)
                .withState(newState)
                .build();
    }

    boolean canUpdateStateFor(Mandate mandate, DirectDebitEvent.SupportedEvent event) {
        return MandateStatesGraph.getStates().getNextStateForEvent(mandate.getState(), event).isPresent();
    }

    private Mandate confirmedDetailsFor(Mandate mandate) {
        updateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);
        mandateDao.updateReferenceAndPaymentProviderId(mandate);
        return mandate;
    }
}
