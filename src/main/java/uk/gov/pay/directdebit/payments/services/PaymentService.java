package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.logging.LoggingKeys;

import javax.inject.Inject;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_ERROR_SUBMITTING_TO_PROVIDER;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.PAYMENT_SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.aPayment;
import static uk.gov.pay.directdebit.payments.model.Payment.PaymentBuilder.fromPayment;

public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentDao paymentDao;
    private final UserNotificationService userNotificationService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final GovUkPayEventService govUkPayEventService;

    @Inject
    public PaymentService(PaymentDao paymentDao,
                          UserNotificationService userNotificationService,
                          PaymentProviderFactory paymentProviderFactory,
                          GovUkPayEventService govUkPayEventService) {
        this.paymentDao = paymentDao;
        this.userNotificationService = userNotificationService;
        this.paymentProviderFactory = paymentProviderFactory;
        this.govUkPayEventService = govUkPayEventService;
    }

    Payment createPayment(long amount, String description, String reference, Mandate mandate) {
        LOGGER.info("Creating payment for mandate {}", kv(LoggingKeys.MANDATE_EXTERNAL_ID, mandate.getExternalId()));
        Payment payment = aPayment()
                .withExternalId(RandomIdGenerator.newId())
                .withAmount(amount)
                .withState(PaymentState.CREATED)
                .withDescription(description)
                .withReference(reference)
                .withMandate(mandate)
                .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC))
                .build();
        Long id = paymentDao.insert(payment);

        Payment insertedPayment = fromPayment(payment).withId(id).build();
        LOGGER.info("Created payment with external id {}", insertedPayment.getExternalId());
        return govUkPayEventService.storeEventAndUpdateStateForPayment(insertedPayment, PAYMENT_CREATED);
    }

    Payment submitPaymentToProvider(Payment payment, PaymentProviderMandateId paymentProviderMandateId) {
        PaymentProviderPaymentIdAndChargeDate providerIdAndChargeDate;
        try {
            providerIdAndChargeDate = paymentProviderFactory
                    .getCommandServiceFor(payment.getMandate().getGatewayAccount().getPaymentProvider())
                    .collect(payment, paymentProviderMandateId);
        } catch (CreatePaymentFailedException e) {
            govUkPayEventService.storeEventAndUpdateStateForPayment(payment, PAYMENT_ERROR_SUBMITTING_TO_PROVIDER);
            throw e;
        }

        Payment submittedPayment = fromPayment(payment)
                .withProviderId(providerIdAndChargeDate.getPaymentProviderPaymentId())
                .withChargeDate(providerIdAndChargeDate.getChargeDate())
                .build();

        paymentDao.updateProviderIdAndChargeDate(submittedPayment);

        userNotificationService.sendPaymentConfirmedEmailFor(submittedPayment);
        return govUkPayEventService.storeEventAndUpdateStateForPayment(submittedPayment, PAYMENT_SUBMITTED);
    }
}
