package uk.gov.pay.directdebit.webhook.gocardless.services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.events.services.GoCardlessEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.MandateQueryService;
import uk.gov.pay.directdebit.mandate.services.MandateStateUpdater;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.payments.services.PaymentStateUpdater;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.SendEmailsForGoCardlessEventsHandler;
import uk.gov.pay.directdebit.webhook.gocardless.services.handlers.UnhandledGoCardlessEventsLogger;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.MANDATES;
import static uk.gov.pay.directdebit.events.model.GoCardlessResourceType.PAYMENTS;

public class WebhookGoCardlessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookGoCardlessService.class);

    private final GoCardlessEventService goCardlessService;
    private final MandateStateUpdater mandateStateUpdater;
    private final PaymentStateUpdater paymentStateUpdater;
    private final MandateQueryService mandateQueryService;
    private final PaymentQueryService paymentQueryService;
    private final SendEmailsForGoCardlessEventsHandler sendEmailsForGoCardlessEventsHandler;
    private final UnhandledGoCardlessEventsLogger unhandledGoCardlessEventsLogger;

    @Inject
    WebhookGoCardlessService(GoCardlessEventService goCardlessService,
                             MandateStateUpdater mandateStateUpdater,
                             PaymentStateUpdater paymentStateUpdater,
                             MandateQueryService mandateQueryService,
                             PaymentQueryService paymentQueryService,
                             SendEmailsForGoCardlessEventsHandler sendEmailsForGoCardlessEventsHandler,
                             UnhandledGoCardlessEventsLogger unhandledGoCardlessEventsLogger) {
        this.goCardlessService = goCardlessService;
        this.sendEmailsForGoCardlessEventsHandler = sendEmailsForGoCardlessEventsHandler;
        this.mandateStateUpdater = mandateStateUpdater;
        this.paymentStateUpdater = paymentStateUpdater;
        this.mandateQueryService = mandateQueryService;
        this.paymentQueryService = paymentQueryService;
        this.unhandledGoCardlessEventsLogger = unhandledGoCardlessEventsLogger;
    }

    public void processEvents(List<GoCardlessEvent> events) {
        long start = new Date().getTime();
        goCardlessService.storeEvents(events);
        long end = new Date().getTime();
        LOGGER.error("Time taken to store all events: {} millis", end - start);
        updateStatesForEvents(events);
        sendEmailsForGoCardlessEventsHandler.sendEmails(events);
        unhandledGoCardlessEventsLogger.logUnhandledEvents(events);
    }

    private void updateStatesForEvents(List<GoCardlessEvent> events) {
        var eventsGroupedByResourceType = events.stream().collect(groupingBy(GoCardlessEvent::getResourceType));
        updateStateForMandateEvents(eventsGroupedByResourceType.getOrDefault(MANDATES, emptyList()));
        updateStateForPaymentEvents(eventsGroupedByResourceType.getOrDefault(PAYMENTS, emptyList()));
    }

    private void updateStateForMandateEvents(List<GoCardlessEvent> eventsThatAffectMandates) {
        eventsThatAffectMandates.stream()
                .map(this::toGoCardlessMandateIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .map(this::getMandate)
                .flatMap(Optional::stream)
                .forEach(mandateStateUpdater::updateStateIfNecessary);
    }

    private Optional<Pair<GoCardlessMandateId, GoCardlessOrganisationId>> toGoCardlessMandateIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessMandateIdAndOrganisationId = goCardlessEvent.getLinksMandate()
                .map(mandateId -> Pair.of(mandateId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error(format("GoCardless event %s has resource_type mandate but no links.mandate", goCardlessEvent.getGoCardlessEventId()));
                    return null;
                });
        return Optional.ofNullable(goCardlessMandateIdAndOrganisationId);
    }

    private Optional<Mandate> getMandate(Pair<GoCardlessMandateId, GoCardlessOrganisationId> goCardlessMandateIdAndOrganisationId) {
        GoCardlessMandateId goCardlessMandateId = goCardlessMandateIdAndOrganisationId.getLeft();
        GoCardlessOrganisationId goCardlessOrganisationId = goCardlessMandateIdAndOrganisationId.getRight();
        try {
            Mandate mandate = mandateQueryService.findByGoCardlessMandateIdAndOrganisationId(
                    goCardlessMandateId,
                    goCardlessOrganisationId);

            return Optional.of(mandate);
        } catch (MandateNotFoundException e) {
            LOGGER.error(format("Could not update status of GoCardless mandate %s for organisation %s because the mandate was not found",
                    goCardlessMandateId,
                    goCardlessOrganisationId));
        }

        return Optional.empty();
    }

    private void updateStateForPaymentEvents(List<GoCardlessEvent> eventsThatAffectPayments) {
        eventsThatAffectPayments.stream()
                .map(this::toGoCardlessPaymentIdAndOrganisationId)
                .flatMap(Optional::stream)
                .distinct()
                .map(this::getPayment)
                .flatMap(Optional::stream)
                .forEach(paymentStateUpdater::updateStateIfNecessary);
    }

    private Optional<Pair<GoCardlessPaymentId, GoCardlessOrganisationId>> toGoCardlessPaymentIdAndOrganisationId(GoCardlessEvent goCardlessEvent) {
        var goCardlessPaymentIdAndOrganisationId = goCardlessEvent.getLinksPayment()
                .map(paymentId -> Pair.of(paymentId, goCardlessEvent.getLinksOrganisation()))
                .orElseGet(() -> {
                    LOGGER.error(format("GoCardless event %s has reource_type payment but no links.payment", goCardlessEvent.getGoCardlessEventId()));
                    return null;
                });
        return Optional.ofNullable(goCardlessPaymentIdAndOrganisationId);
    }

    private Optional<Payment> getPayment(Pair<GoCardlessPaymentId, GoCardlessOrganisationId> goCardlessPaymentIdAndOrganisationId) {
        GoCardlessPaymentId goCardlessPaymentId = goCardlessPaymentIdAndOrganisationId.getLeft();
        GoCardlessOrganisationId goCardlessOrganisationId = goCardlessPaymentIdAndOrganisationId.getRight();
        return paymentQueryService.findByGoCardlessPaymentIdAndOrganisationId(goCardlessPaymentId, goCardlessOrganisationId)
                .or(() -> {
                    LOGGER.error(format("Could not update status of GoCardless payment %s for organisation %s because the payment was not found",
                            goCardlessPaymentId,
                            goCardlessOrganisationId));
                    return Optional.empty();
                });
    }
}
