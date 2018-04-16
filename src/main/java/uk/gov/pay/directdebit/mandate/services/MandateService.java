package uk.gov.pay.directdebit.mandate.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;

import javax.inject.Inject;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.MANDATE;

public class MandateService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final PaymentRequestEventService paymentRequestEventService;
    private final UserNotificationService userNotificationService;

    @Inject
    public MandateService(MandateDao mandateDao, PaymentRequestEventService paymentRequestEventService, UserNotificationService userNotificationService) {
        this.paymentRequestEventService = paymentRequestEventService;
        this.mandateDao = mandateDao;
        this.userNotificationService = userNotificationService;
    }


    public Mandate findMandateForTransactionId(Long transactionId) {
        return mandateDao
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new MandateNotFoundException(transactionId.toString()));
    }


    public PaymentRequestEvent mandateFailedFor(Transaction transaction, Payer payer) {
        Mandate oldMandate = findMandateForTransactionId(transaction.getId());
        updateStateFor(oldMandate, MANDATE_FAILED);
        userNotificationService.sendMandateFailedEmailFor(transaction, payer);
        return paymentRequestEventService.registerMandateFailedEventFor(transaction);
    }

    public PaymentRequestEvent mandateCancelledFor(Transaction transaction, Payer payer) {
        Mandate oldMandate = findMandateForTransactionId(transaction.getId());
        Mandate newMandate = updateStateFor(oldMandate, MANDATE_CANCELLED);
        userNotificationService.sendMandateCancelledEmailFor(transaction, newMandate, payer);
        return paymentRequestEventService.registerMandateCancelledEventFor(transaction);
    }


    public PaymentRequestEvent mandatePendingFor(Transaction transaction) {
        return paymentRequestEventService.registerMandatePendingEventFor(transaction);
    }

    public PaymentRequestEvent mandateActiveFor(Transaction transaction) {
        Mandate mandate = findMandateForTransactionId(transaction.getId());
        updateStateFor(mandate, MANDATE_ACTIVE);
        return paymentRequestEventService.registerMandateActiveEventFor(transaction);
    }

    private Mandate updateStateFor(Mandate mandate, SupportedEvent event) {
        MandateState newState = MandateStatesGraph.getStates().getNextStateForEvent(mandate.getState(),
                event);
        mandateDao.updateState(mandate.getId(), newState);
        LOGGER.info("Updating mandate {} - from {} to {}",
                mandate.getExternalId(),
                mandate.getState(),
                newState);
        mandate.setState(newState);
        return mandate;
    }

    public Optional<PaymentRequestEvent> findMandatePendingEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getPaymentRequestId(), MANDATE, MANDATE_PENDING);
    }
}
