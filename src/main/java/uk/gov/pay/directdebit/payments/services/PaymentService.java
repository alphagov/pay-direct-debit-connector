package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.events.services.DirectDebitEventService;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;

import javax.inject.Inject;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.aPayment;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;

public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentDao paymentDao;
    private final DirectDebitEventService directDebitEventService;
    private final UserNotificationService userNotificationService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final GovUkPayEventService govUkPayEventService;

    @Inject
    public PaymentService(PaymentDao paymentDao,
                          DirectDebitEventService directDebitEventService,
                          UserNotificationService userNotificationService,
                          PaymentProviderFactory paymentProviderFactory,
                          GovUkPayEventService govUkPayEventService) {
        this.directDebitEventService = directDebitEventService;
        this.paymentDao = paymentDao;
        this.userNotificationService = userNotificationService;
        this.paymentProviderFactory = paymentProviderFactory;
        this.govUkPayEventService = govUkPayEventService;
    }

    public Payment findPaymentForExternalId(String externalId) {
        Payment payment = paymentDao.findByExternalId(externalId)
                .orElseThrow(() -> new ChargeNotFoundException("No charges found for payment id: " + externalId));
        LOGGER.info("Found charge for payment with id: {}", externalId);
        return payment;
    }

    Payment createPayment(long amount, String description, String reference, Mandate mandate) {
        LOGGER.info("Creating payment for mandate {}", mandate.getExternalId());
        Payment payment = aPayment()
                .withExternalId(RandomIdGenerator.newId())
                .withAmount(amount)
                .withState(PaymentStatesGraph.initialState())
                .withDescription(description)
                .withReference(reference)
                .withMandate(mandate)
                .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
        Long id = paymentDao.insert(payment);

        Payment insertedPayment = fromPayment(payment).withId(id).build();
        paymentCreatedFor(insertedPayment);
        LOGGER.info("Created payment with external id {}", insertedPayment.getExternalId());
        return insertedPayment;
    }

    Payment submitPaymentToProvider(Payment payment) {
        var providerPaymentIdAndChargeDate = paymentProviderFactory
                .getCommandServiceFor(payment.getMandate().getGatewayAccount().getPaymentProvider())
                .collect(payment.getMandate(), payment);

        Payment submittedPayment = fromPayment(payment)
                .withProviderId(providerPaymentIdAndChargeDate.getPaymentProviderPaymentId())
                .withChargeDate(providerPaymentIdAndChargeDate.getChargeDate())
                .build();

        return paymentSubmittedToProviderFor(submittedPayment);
    }

    public PaymentResponse getPaymentWithExternalId(String paymentExternalId) {
        Payment payment = findPaymentForExternalId(paymentExternalId);
        return PaymentResponse.from(payment);
    }


    public List<Payment> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return paymentDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }

    public List<Payment> findAllPaymentsBySetOfStatesAndCreationTime(Set<PaymentState> states, ZonedDateTime creationTime) {
        return paymentDao.findAllPaymentsBySetOfStatesAndCreationTime(states, creationTime);
    }

    // todo we might want to split this service in query / state update like mandate
    public List<Payment> findPaymentsForMandate(MandateExternalId mandateExternalId) {
        return paymentDao.findAllByMandateExternalId(mandateExternalId);
    }

    public DirectDebitEvent paymentCreatedFor(Payment payment) {
        return directDebitEventService.registerPaymentCreatedEventFor(payment);
    }

    // TODO: a remnant from one-off payments, should be removed
    public DirectDebitEvent paymentExpired(Payment payment) {
        return directDebitEventService.registerPaymentExpiredEventFor(payment);
    }

    public Payment paymentSubmittedToProviderFor(Payment payment) {
        userNotificationService.sendPaymentConfirmedEmailFor(payment);
        return govUkPayEventService.storeEventAndUpdateStateForPayment(payment, PAYMENT_SUBMITTED);
    }

    public DirectDebitEvent paymentFailedWithEmailFor(Payment payment) {
        userNotificationService.sendPaymentFailedEmailFor(payment);
        return paymentFailedFor(payment);
    }

    public DirectDebitEvent paymentFailedWithoutEmailFor(Payment payment) {
        return paymentFailedFor(payment);
    }

    private DirectDebitEvent paymentFailedFor(Payment payment) {
        return directDebitEventService.registerPaymentFailedEventFor(payment);
    }

    public DirectDebitEvent paymentPaidOutFor(Payment payment) {
        return directDebitEventService.registerPaymentPaidOutEventFor(payment);
    }

    public DirectDebitEvent paymentAcknowledgedFor(Payment payment) {
        return directDebitEventService.registerPaymentAcknowledgedEventFor(payment);
    }

    public DirectDebitEvent paymentSubmittedFor(Payment payment) {
        return directDebitEventService.registerPaymentSubmittedEventFor(payment);
    }

    public DirectDebitEvent payoutPaidFor(Payment payment) {
        return directDebitEventService.registerPayoutPaidEventFor(payment);
    }

    public Optional<DirectDebitEvent> findPaymentSubmittedEventFor(Payment payment) {
        return directDebitEventService.findBy(payment.getId(), CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }
}
