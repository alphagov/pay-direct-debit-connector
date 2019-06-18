package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentRequest;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.CollectRequest;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;
import static uk.gov.pay.directdebit.payments.resources.PaymentResource.CHARGE_API_PATH;

public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);
    private final TokenService tokenService;
    private final GatewayAccountDao gatewayAccountDao;
    private final DirectDebitConfig directDebitConfig;
    private final PaymentDao paymentDao;
    private final DirectDebitEventService directDebitEventService;
    private final UserNotificationService userNotificationService;
    private final PaymentProviderFactory paymentProviderFactory;

    @Inject
    public PaymentService(TokenService tokenService,
                          GatewayAccountDao gatewayAccountDao,
                          DirectDebitConfig directDebitConfig,
                          PaymentDao paymentDao,
                          DirectDebitEventService directDebitEventService,
                          UserNotificationService userNotificationService, PaymentProviderFactory paymentProviderFactory) {
        this.tokenService = tokenService;
        this.gatewayAccountDao = gatewayAccountDao;
        this.directDebitConfig = directDebitConfig;
        this.directDebitEventService = directDebitEventService;
        this.paymentDao = paymentDao;
        this.userNotificationService = userNotificationService;
        this.paymentProviderFactory = paymentProviderFactory;
    }

    public Payment findPaymentForExternalId(String externalId) {
        Payment payment = paymentDao.findByExternalId(externalId)
                .orElseThrow(() -> new ChargeNotFoundException("No charges found for payment id: " + externalId));
        LOGGER.info("Found charge for payment with id: {}", externalId);
        return payment;
    }

    public Payment createPayment(GatewayAccount gatewayAccount, Mandate mandate,
                                 CollectPaymentRequest collectPaymentRequest) {

        Payment payment = createPayment(collectPaymentRequest, mandate, gatewayAccount.getExternalId());

        LocalDate chargeDate = paymentProviderFactory
                .getCommandServiceFor(gatewayAccount.getPaymentProvider())
                .collect(mandate, payment);

        onDemandPaymentSubmittedToProviderFor(payment, chargeDate);

        return payment;
    }
    
    public Payment createPayment(CollectRequest collectRequest, Mandate mandate, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    LOGGER.info("Creating payment for mandate {}", mandate.getExternalId());
                    Payment payment = new Payment(
                            collectRequest.getAmount(),
                            PaymentStatesGraph.initialState(),
                            collectRequest.getDescription(),
                            collectRequest.getReference(),
                            mandate,
                            ZonedDateTime.now(ZoneOffset.UTC)
                    );
                    Long id = paymentDao.insert(payment);
                    payment.setId(id);
                    paymentCreatedFor(payment);
                    LOGGER.info("Created payment with external id {}", payment.getExternalId());
                    return payment;
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public PaymentResponse createPaymentResponseWithAllLinks(Payment payment, String accountExternalId, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, CHARGE_API_PATH, accountExternalId, payment.getExternalId())));

        if (!payment.getState().toExternal().isFinished()) {
            Token token = tokenService.generateNewTokenFor(payment.getMandate());
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(directDebitConfig.getLinks().getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(directDebitConfig.getLinks().getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("chargeTokenId", token.getToken())));
        }
        return PaymentResponse.from(payment, dataLinks);
    }

    public CollectPaymentResponse collectPaymentResponseWithSelfLink(Payment payment, String accountExternalId, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = ImmutableList.of(
                createLink("self", GET, selfUriFor(uriInfo, "/v1/api/accounts/{accountId}/charges/{paymentExternalId}",
                        accountExternalId, payment.getExternalId()))
        );
        return CollectPaymentResponse.from(payment, dataLinks);
    }

    public PaymentResponse getPaymentWithExternalId(String accountExternalId, String paymentExternalId, UriInfo uriInfo) {
        Payment payment = findPaymentForExternalId(paymentExternalId);
        return createPaymentResponseWithAllLinks(payment, accountExternalId, uriInfo);
    }


    public List<Payment> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return paymentDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }

    public List<Payment> findAllPaymentsBySetOfStatesAndCreationTime(Set<PaymentState> states, ZonedDateTime creationTime) {
        return paymentDao.findAllPaymentsBySetOfStatesAndCreationTime(states, creationTime);
    }

    public Payment findPayment(Long paymentId) {
        return paymentDao
                .findById(paymentId)
                .orElseThrow(() -> new ChargeNotFoundException("payment id" + paymentId.toString()));
    }
    
    public Optional<Payment> findPaymentByProviderId(PaymentProvider paymentProvider, PaymentProviderPaymentId providerId) {
        return paymentDao.findPaymentByProviderId(paymentProvider, providerId);
    }

    // todo we might want to split this service in query / state update like mandate
    public List<Payment> findPaymentsForMandate(MandateExternalId mandateExternalId) {
        return paymentDao.findAllByMandateExternalId(mandateExternalId);
    }

    public DirectDebitEvent paymentCreatedFor(Payment payment) {
        return directDebitEventService.registerPaymentCreatedEventFor(payment);
    }
    
    public DirectDebitEvent paymentExpired(Payment payment) {
        Payment updatePayment = updateStateFor(payment, SupportedEvent.PAYMENT_EXPIRED_BY_SYSTEM);
        return directDebitEventService.registerPaymentExpiredEventFor(updatePayment);
    }

    public DirectDebitEvent oneOffPaymentSubmittedToProviderFor(Payment payment, LocalDate earliestChargeDate) {
        updateStateFor(payment, PAYMENT_SUBMITTED_TO_PROVIDER);
        userNotificationService.sendOneOffPaymentConfirmedEmailFor(payment, earliestChargeDate);
        return directDebitEventService.registerPaymentSubmittedToProviderEventFor(payment);
    }

    public DirectDebitEvent onDemandPaymentSubmittedToProviderFor(Payment payment, LocalDate earliestChargeDate) {
        updateStateFor(payment, PAYMENT_SUBMITTED_TO_PROVIDER);
        userNotificationService.sendOnDemandPaymentConfirmedEmailFor(payment, earliestChargeDate);
        return directDebitEventService.registerPaymentSubmittedToProviderEventFor(payment);
    }

    public DirectDebitEvent paymentFailedWithEmailFor(Payment payment) {
        userNotificationService.sendPaymentFailedEmailFor(payment);
        return paymentFailedFor(payment);
    }

    public DirectDebitEvent paymentFailedWithoutEmailFor(Payment payment) {
        return paymentFailedFor(payment);
    }

    private DirectDebitEvent paymentFailedFor(Payment payment) {
        Payment updatedPayment = updateStateFor(payment, SupportedEvent.PAYMENT_FAILED);
        return directDebitEventService.registerPaymentFailedEventFor(updatedPayment);
    }

    public DirectDebitEvent paymentPaidOutFor(Payment payment) {
        Payment updatedPayment = updateStateFor(payment, PAID_OUT);
        return directDebitEventService.registerPaymentPaidOutEventFor(updatedPayment);
    }

    public DirectDebitEvent paymentAcknowledgedFor(Payment payment) {
        return directDebitEventService.registerPaymentAcknowledgedEventFor(payment);
    }

    public DirectDebitEvent paymentCancelledFor(Payment payment) {
        Payment newPayment = updateStateFor(payment, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        return directDebitEventService
                .registerPaymentCancelledEventFor(payment.getMandate(), newPayment);
    }

    public DirectDebitEvent paymentMethodChangedFor(Payment payment) {
        Payment newPayment = updateStateFor(payment, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        return directDebitEventService.registerPaymentMethodChangedEventFor(newPayment.getMandate());
    }

    public DirectDebitEvent paymentSubmittedFor(Payment payment) {
        return directDebitEventService.registerPaymentSubmittedEventFor(payment);
    }

    public DirectDebitEvent payoutPaidFor(Payment payment) {
        return directDebitEventService.registerPayoutPaidEventFor(payment);
    }

    private Payment updateStateFor(Payment payment, SupportedEvent event) {
        PaymentState newState = getStates()
                .getNextStateForEvent(payment.getState(), event)
                .orElseThrow(() -> new InvalidStateTransitionException(event.toString(), payment.getState().toString()));

        paymentDao.updateState(payment.getId(), newState);
        LOGGER.info("Updated payment {} - from {} to {}",
                payment.getExternalId(),
                payment.getState(),
                newState);
        payment.setState(newState);
        return payment;
    }

    public Optional<DirectDebitEvent> findPaymentSubmittedEventFor(Payment payment) {
        return directDebitEventService.findBy(payment.getId(), CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }
}
