package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.GoCardlessService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.webhook.gocardless.api.WebhookParser;

import javax.inject.Inject;
import java.util.List;

public class WebhookGoCardlessService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(WebhookGoCardlessService.class);
    private final GoCardlessEventDao goCardlessEventDao;
    private final TransactionService transactionService;
    private final GoCardlessService goCardlessService;

    @Inject
    public WebhookGoCardlessService(GoCardlessEventDao goCardlessEventDao, GoCardlessService goCardlessService, TransactionService transactionService) {
        this.goCardlessEventDao = goCardlessEventDao;
        this.goCardlessService = goCardlessService;
        this.transactionService = transactionService;
    }

    public void handleEvents(List<GoCardlessEvent> events) {
        events.forEach((event -> {
            goCardlessEventDao.insert(event);
            LOGGER.info("inserted gocardless event with id {} ", event.getEventId());
            handle(event);
        }));
    }

    private GoCardlessEvent handle(GoCardlessEvent event) {
        WebhookParser.HandledResourceType resourceType = WebhookParser.HandledResourceType.fromString(event.getResourceType());
        if (resourceType != null) {
            switch (resourceType) {
                case PAYMENTS:
                    handlePayment(event);
                case MANDATES:
                    handleMandate(event);
            }
        }
        return event;
    }

    private void handlePayment(GoCardlessEvent event) {
        GoCardlessAction goCardlessAction = GoCardlessPaymentAction.fromString(event.getAction());
        if (goCardlessAction != null) {
            GoCardlessPayment goCardlessPayment = goCardlessService.findPaymentForEvent(event);
            Transaction transaction = transactionService.findChargeFor(goCardlessPayment.getTransactionId());
            updateTransaction(goCardlessAction, event, transaction);
            LOGGER.info("handled gocardless payment event with id {} ", event.getEventId());
        }
    }
    private void handleMandate(GoCardlessEvent event) {
        GoCardlessAction goCardlessAction = GoCardlessMandateAction.fromString(event.getAction());
        if (goCardlessAction != null) {
            GoCardlessMandate goCardlessMandate = goCardlessService.findMandateForEvent(event);
            Transaction transaction = transactionService.findChargeForMandateId(goCardlessMandate.getMandateId());
            updateTransaction(goCardlessAction, event, transaction);
            LOGGER.info("handled gocardless mandate event with id {} ", event.getEventId());
        }
    }

    private void updateTransaction(GoCardlessAction goCardlessAction, GoCardlessEvent goCardlessEvent, Transaction transaction) {
            PaymentRequestEvent paymentRequestEvent = goCardlessAction.changeChargeState(transactionService, transaction);
            goCardlessEventDao.updatePaymentRequestEventId(goCardlessEvent.getId(), paymentRequestEvent.getId());
    }
}
