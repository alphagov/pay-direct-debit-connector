package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.api.PaymentRequestFrontendResponse;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.Event;
import uk.gov.pay.directdebit.payments.model.Event.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.pay.directdebit.common.util.URIBuilder.createLink;
import static uk.gov.pay.directdebit.common.util.URIBuilder.nextUrl;
import static uk.gov.pay.directdebit.common.util.URIBuilder.selfUriFor;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.Event.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.Event.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.Event.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;
import static uk.gov.pay.directdebit.payments.resources.PaymentRequestResource.CHARGE_API_PATH;

public class TransactionService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(TransactionService.class);
    private final TokenService tokenService;
    private final GatewayAccountDao gatewayAccountDao;
    private final DirectDebitConfig directDebitConfig;
    private final TransactionDao transactionDao;
    private final PaymentRequestEventService paymentRequestEventService;
    private final UserNotificationService userNotificationService;
    private final CreatePaymentParser createPaymentParser;
    @Inject
    public TransactionService(TokenService tokenService,
            GatewayAccountDao gatewayAccountDao,
            DirectDebitConfig directDebitConfig,
            TransactionDao transactionDao,
            PaymentRequestEventService paymentRequestEventService,
            UserNotificationService userNotificationService,
            CreatePaymentParser createPaymentParser) {
        this.tokenService = tokenService;
        this.gatewayAccountDao = gatewayAccountDao;
        this.directDebitConfig = directDebitConfig;
        this.paymentRequestEventService = paymentRequestEventService;
        this.transactionDao = transactionDao;
        this.userNotificationService = userNotificationService;
        this.createPaymentParser = createPaymentParser;
    }

    public Transaction findTransactionForExternalIdAndGatewayAccountExternalId(String externalId, String accountExternalId) {
        Transaction transaction = transactionDao.findByExternalId(externalId)
                .orElseThrow(() -> new ChargeNotFoundException("No charges found for transaction id: " + externalId));
        LOGGER.info("Found charge for transaction with id: {} for gateway account id: {}", externalId, accountExternalId);
        return transaction;
    }

    private PaymentRequestResponse populateResponseWith(String accountExternalId, Transaction transaction, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = new ArrayList<>();

        dataLinks.add(createLink("self", GET, selfUriFor(uriInfo, CHARGE_API_PATH, accountExternalId, transaction.getExternalId())));

        if (!transaction.getState().toExternal().isFinished()) {
            Token token = tokenService.generateNewTokenFor(transaction.getMandate());
            dataLinks.add(createLink("next_url",
                    GET,
                    nextUrl(directDebitConfig.getLinks().getFrontendUrl(), "secure", token.getToken())));
            dataLinks.add(createLink("next_url_post",
                    POST,
                    nextUrl(directDebitConfig.getLinks().getFrontendUrl(), "secure"),
                    APPLICATION_FORM_URLENCODED,
                    ImmutableMap.of("chargeTokenId", token.getToken())));
        }
        return new PaymentRequestResponse(
                transaction.getExternalId(),
                transaction.getState().toExternal(),
                transaction.getAmount(),
                transaction.getMandate().getReturnUrl(),
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getCreatedDate().toString(),
                dataLinks);
    }
    
    public PaymentRequestResponse createTransaction(Map<String, String> createPaymentRequest, Mandate mandate, String accountExternalId, UriInfo uriInfo) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    LOGGER.info("Creating transaction for mandate {}", mandate.getExternalId());
                    Transaction transaction = createPaymentParser
                            .parse(createPaymentRequest, mandate);
                    Long id = transactionDao.insert(transaction);
                    transaction.setId(id);
                    paymentRequestEventService.insertEventFor(mandate, chargeCreated(mandate.getId(), transaction.getId()));
                    LOGGER.info("Created transaction with external id {}", transaction.getExternalId());
                    return populateResponseWith(accountExternalId, transaction, uriInfo);
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public PaymentRequestResponse getPaymentWithExternalId(String accountExternalId, String paymentExternalId, UriInfo uriInfo) {
        Transaction transaction = findTransactionForExternalIdAndGatewayAccountExternalId(
                paymentExternalId, accountExternalId);
        return populateResponseWith(accountExternalId, transaction, uriInfo);
    }
    

    public List<Transaction> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return transactionDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }
    
    public Transaction findTransaction(Long transactionId) {
        return transactionDao
                .findById(transactionId)
                .orElseThrow(() -> new ChargeNotFoundException("transaction id" + transactionId.toString()));
    }

    public List<Transaction> findTransactionsForMandate(String mandateExternalId) {
        return transactionDao.findAllByMandateExternalId(mandateExternalId);
    }

    public Event paymentSubmittedToProviderFor(Transaction transaction, LocalDate earliestChargeDate) {
        updateStateFor(transaction,
                PAYMENT_SUBMITTED_TO_PROVIDER);
        userNotificationService.sendPaymentConfirmedEmailFor(transaction, earliestChargeDate);
        return paymentRequestEventService.registerPaymentSubmittedToProviderEventFor(transaction);
    }

    public Event paymentFailedWithEmailFor(Transaction transaction) {
        userNotificationService.sendPaymentFailedEmailFor(transaction);
        return paymentFailedFor(transaction);
    }

    public Event paymentFailedWithoutEmailFor(Transaction transaction) {
        return paymentFailedFor(transaction);
    }

    private Event paymentFailedFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_FAILED);
        return paymentRequestEventService.registerPaymentFailedEventFor(updatedTransaction);
    }

    public Event paymentPaidOutFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAID_OUT);
        return paymentRequestEventService.registerPaymentPaidOutEventFor(updatedTransaction);
    }

    public Event paymentAcknowledgedFor(Transaction transaction) {
        return paymentRequestEventService.registerPaymentAcknowledgedEventFor(transaction);
    }

    public Event paymentCancelledFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        return paymentRequestEventService.registerPaymentCancelledEventFor(transaction.getMandate(), newTransaction);
    }

    public Event paymentMethodChangedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        return paymentRequestEventService.registerPaymentMethodChangedEventFor(newTransaction.getMandate());
    }

    public Event paymentSubmittedFor(Transaction transaction) {
        return paymentRequestEventService.registerPaymentSubmittedEventFor(transaction);
    }

    public Event payoutPaidFor(Transaction transaction) {
        return paymentRequestEventService.registerPayoutPaidEventFor(transaction);
    }

    private Transaction updateStateFor(Transaction transaction, SupportedEvent event) {
        PaymentState newState = getStates().getNextStateForEvent(transaction.getState(),
                event);
        transactionDao.updateState(transaction.getId(), newState);
        LOGGER.info("Updated transaction {} - from {} to {}",
                transaction.getExternalId(),
                transaction.getState(),
                newState);
        transaction.setState(newState);
        return transaction;
    }

    public Optional<Event> findPaymentSubmittedEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getId(), CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }

}
