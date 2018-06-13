package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.mandate.api.CreateMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.exception.TransactionConflictException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateStatesGraph;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.model.Event;
import uk.gov.pay.directdebit.payments.model.Event.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_ACTIVE;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_CANCELLED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_FAILED;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.Event.Type.MANDATE;

public class MandateService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(MandateService.class);
    private final MandateDao mandateDao;
    private final LinksConfig linksConfig;
    private final GatewayAccountDao gatewayAccountDao;
    private final TokenService tokenService;
    private final TransactionService transactionService;
    private final PaymentRequestEventService paymentRequestEventService;
    private final UserNotificationService userNotificationService;

    @Inject
    public MandateService(
            DirectDebitConfig directDebitConfig,
            MandateDao mandateDao, GatewayAccountDao gatewayAccountDao,
            TokenService tokenService,
            TransactionService transactionService,
            PaymentRequestEventService paymentRequestEventService,
            UserNotificationService userNotificationService) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.tokenService = tokenService;
        this.transactionService = transactionService;
        this.paymentRequestEventService = paymentRequestEventService;
        this.mandateDao = mandateDao;
        this.userNotificationService = userNotificationService;
        this.linksConfig = directDebitConfig.getLinks();
    }

    public Mandate createMandate(Map<String, String> mandateRequestMap, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    Mandate mandate = new Mandate(
                            gatewayAccount,
                            MandateType.valueOf(mandateRequestMap.get("agreement_type")),
                            MandateState.CREATED,
                            mandateRequestMap.get("return_url"),
                            ZonedDateTime.now(ZoneOffset.UTC),
                            null);
                    LOGGER.info("Creating mandate external id {}", mandate.getExternalId());
                    Long id = mandateDao.insert(mandate);
                    mandate.setId(id);
                    return mandate;
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public Mandate validateMandateWithToken(String token) {
        return findMandateForToken(token)
                .orElseThrow(TokenNotFoundException::new);
    }
    public CreateMandateResponse createMandateResponse(Map<String, String> mandateRequestMap, String accountExternalId, UriInfo uriInfo) {
        Mandate mandate = createMandate(mandateRequestMap, accountExternalId);
        String mandateExternalId = mandate.getExternalId();
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, 
                "/v1/api/accounts/{accountId}/mandates/{mandateExternalId}", 
                accountExternalId, 
                mandateExternalId)));

        if (!mandate.getState().toExternal().isFinished()) {
            Token token = tokenService.generateNewTokenFor(mandate);
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(linksConfig.getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(linksConfig.getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("chargeTokenId", token.getToken())));
        }
        return new CreateMandateResponse(
                mandateExternalId,
                mandate.getType(),
                mandate.getReturnUrl(),
                mandate.getCreatedDate().toString(),
                mandate.getState().toExternal(),
                dataLinks);
    }

    public Optional<Mandate> findMandateForToken(String token) {
        return mandateDao
                .findByTokenId(token).map(mandate -> {
                    Mandate newMandate = updateStateFor(mandate, TOKEN_EXCHANGED);
                    paymentRequestEventService.registerTokenExchangedEventFor(newMandate);
                    return newMandate;
                });
    }
    public Mandate findByExternalId(String externalId) {
        return mandateDao
                .findByExternalId(externalId)
                .orElseThrow(() -> new MandateNotFoundException(externalId)); 
    }

    public Mandate findById(Long id) {
        return mandateDao
                .findById(id)
                .orElseThrow(() -> new MandateNotFoundException(id.toString()));
    }

    public Event mandateFailedFor(Mandate oldMandate) {
        Mandate newMandate = updateStateFor(oldMandate, MANDATE_FAILED);
        userNotificationService.sendMandateFailedEmailFor(newMandate);
        return paymentRequestEventService.registerMandateFailedEventFor(newMandate);
    }

    public Event mandateCancelledFor(Mandate oldMandate) {
        Mandate newMandate = updateStateFor(oldMandate, MANDATE_CANCELLED);
        userNotificationService.sendMandateCancelledEmailFor(newMandate);
        return paymentRequestEventService.registerMandateCancelledEventFor(newMandate);
    }
    
    public Event mandatePendingFor(Mandate oldMandate) {
        Mandate newMandate = updateStateFor(oldMandate, MANDATE_PENDING);
        return paymentRequestEventService.registerMandatePendingEventFor(newMandate);
    }

    public Event awaitingDirectDebitDetailsFor(Mandate mandate) {
        return paymentRequestEventService.registerAwaitingDirectDebitDetailsEventFor(mandate);
    }

    
    public Event mandateActiveFor(Mandate mandate) {
        updateStateFor(mandate, MANDATE_ACTIVE);
        return paymentRequestEventService.registerMandateActiveEventFor(mandate);
    }
    
    public Mandate receiveDirectDebitDetailsFor(String mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        paymentRequestEventService.registerDirectDebitReceivedEventFor(mandate);
        return mandate;
    }

    public Mandate confirmedDirectDebitDetailsFor(String mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        updateStateFor(mandate, DIRECT_DEBIT_DETAILS_CONFIRMED);
        paymentRequestEventService.registerDirectDebitConfirmedEventFor(mandate);
        return mandate;
    }

    public Mandate payerCreatedFor(Mandate mandate) {
        paymentRequestEventService.registerPayerCreatedEventFor(mandate);
        return mandate;
    }

    public Event payerEditedFor(Mandate mandate) {
        return paymentRequestEventService.registerPayerEditedEventFor(mandate);
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

    public Optional<Event> findMandatePendingEventFor(Mandate mandate) {
        return paymentRequestEventService.findBy(mandate.getId(), MANDATE, MANDATE_PENDING);
    }

    public Event changePaymentMethodFor(String mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        if (mandate.getType().equals(MandateType.ONE_OFF)) {
            Transaction transaction = retrieveTransactionForOneOffMandate(mandateExternalId);
            transactionService.paymentMethodChangedFor(transaction);
        }
        Mandate newMandate = updateStateFor(mandate, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        return paymentRequestEventService.registerPaymentMethodChangedEventFor(newMandate);
    }

    public Event cancelMandateCreation(String mandateExternalId) {
        Mandate mandate = findByExternalId(mandateExternalId);
        if (mandate.getType().equals(MandateType.ONE_OFF)) {
            Transaction transaction = retrieveTransactionForOneOffMandate(mandateExternalId);
            transactionService.paymentCancelledFor(transaction);
        }
        Mandate newMandate = updateStateFor(mandate, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        return paymentRequestEventService.registerMandateCancelledEventFor(newMandate);
    }
    
    private Transaction retrieveTransactionForOneOffMandate(String mandateExternalId) {
        List<Transaction> transactions = transactionService.findTransactionsForMandate(mandateExternalId);
        if (transactions.size() != 1) {
            throw new TransactionConflictException("Found multiple transactions for one off mandate with external id " + mandateExternalId);
        }
        return transactions.get(0);
    }
}
