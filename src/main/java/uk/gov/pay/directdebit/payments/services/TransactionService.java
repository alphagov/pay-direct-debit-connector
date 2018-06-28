package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.TransactionResponse;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
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
import static uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate.ONE_OFF_PAYMENT_CONFIRMED;
import static uk.gov.pay.directdebit.notifications.model.EmailPayload.EmailTemplate.ON_DEMAND_PAYMENT_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_BANK;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.PAYMENT_SUBMITTED_TO_PROVIDER;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;
import static uk.gov.pay.directdebit.payments.resources.TransactionResource.CHARGE_API_PATH;

public class TransactionService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(TransactionService.class);
    private final TokenService tokenService;
    private final GatewayAccountDao gatewayAccountDao;
    private final DirectDebitConfig directDebitConfig;
    private final TransactionDao transactionDao;
    private final DirectDebitEventService directDebitEventService;
    private final UserNotificationService userNotificationService;
    private final CreatePaymentParser createPaymentParser;

    @Inject
    public TransactionService(TokenService tokenService,
            GatewayAccountDao gatewayAccountDao,
            DirectDebitConfig directDebitConfig,
            TransactionDao transactionDao,
            DirectDebitEventService directDebitEventService,
            UserNotificationService userNotificationService,
            CreatePaymentParser createPaymentParser) {
        this.tokenService = tokenService;
        this.gatewayAccountDao = gatewayAccountDao;
        this.directDebitConfig = directDebitConfig;
        this.directDebitEventService = directDebitEventService;
        this.transactionDao = transactionDao;
        this.userNotificationService = userNotificationService;
        this.createPaymentParser = createPaymentParser;
    }

    public Transaction findTransactionForExternalId(String externalId) {
        Transaction transaction = transactionDao.findByExternalId(externalId)
                .orElseThrow(() -> new ChargeNotFoundException("No charges found for transaction id: " + externalId));
        LOGGER.info("Found charge for transaction with id: {}", externalId);
        return transaction;
    }

    public Transaction createTransaction(Map<String, String> createTransaction, Mandate mandate, String accountExternalId) {
        return gatewayAccountDao.findByExternalId(accountExternalId)
                .map(gatewayAccount -> {
                    LOGGER.info("Creating transaction for mandate {}", mandate.getExternalId());
                    Transaction transaction = createPaymentParser
                            .parse(createTransaction, mandate);
                    Long id = transactionDao.insert(transaction);
                    transaction.setId(id);
                    directDebitEventService
                            .insertEventFor(mandate, chargeCreated(mandate.getId(), transaction.getId()));
                    LOGGER.info("Created transaction with external id {}", transaction.getExternalId());
                    return transaction;
                })
                .orElseThrow(() -> {
                    LOGGER.error("Gateway account with id {} not found", accountExternalId);
                    return new GatewayAccountNotFoundException(accountExternalId);
                });
    }

    public TransactionResponse createPaymentResponseWithAllLinks(Transaction transaction, String accountExternalId, UriInfo uriInfo) {
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
        return TransactionResponse.from(transaction, dataLinks);
    }

    public CollectPaymentResponse collectPaymentResponseWithSelfLink(Transaction transaction, String accountExternalId, UriInfo uriInfo) {
        List<Map<String, Object>> dataLinks = ImmutableList.of(
                createLink("self", GET, selfUriFor(uriInfo, "/v1/api/accounts/{accountId}/charges/{transactionExternalId}",
                        accountExternalId, transaction.getExternalId()))
        );
        return CollectPaymentResponse.from(transaction, dataLinks);
    }
    
    public TransactionResponse getPaymentWithExternalId(String accountExternalId, String paymentExternalId, UriInfo uriInfo) {
        Transaction transaction = findTransactionForExternalId(paymentExternalId);
        return createPaymentResponseWithAllLinks(transaction, accountExternalId, uriInfo);
    }


    public List<Transaction> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return transactionDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }

    public List<Transaction> findAllPaymentsBySetOfStatesAndCreationTime(Set<PaymentState> states, ZonedDateTime creationTime) {
        return transactionDao.findAllPaymentsBySetOfStatesAndCreationTime(states, creationTime);
    }

    public Transaction findTransaction(Long transactionId) {
        return transactionDao
                .findById(transactionId)
                .orElseThrow(() -> new ChargeNotFoundException("transaction id" + transactionId.toString()));
    }

    public List<Transaction> findTransactionsForMandate(String mandateExternalId) {
        return transactionDao.findAllByMandateExternalId(mandateExternalId);
    }

    public DirectDebitEvent paymentExpired(Transaction transaction) {
        Transaction updateTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_EXPIRED_BY_SYSTEM);
        return directDebitEventService.registerPaymentExpiredEventFor(updateTransaction);
    }

    public DirectDebitEvent oneOffPaymentSubmittedToProviderFor(Transaction transaction, LocalDate earliestChargeDate) {
        updateStateFor(transaction, PAYMENT_SUBMITTED_TO_PROVIDER);
        userNotificationService.sendPaymentConfirmedEmailFor(ONE_OFF_PAYMENT_CONFIRMED, transaction, earliestChargeDate);
        return directDebitEventService.registerPaymentSubmittedToProviderEventFor(transaction);
    }

    public DirectDebitEvent onDemandPaymentSubmittedToProviderFor(Transaction transaction, LocalDate earliestChargeDate) {
        updateStateFor(transaction, PAYMENT_SUBMITTED_TO_PROVIDER);
        userNotificationService.sendPaymentConfirmedEmailFor(ON_DEMAND_PAYMENT_CONFIRMED, transaction, earliestChargeDate);
        return directDebitEventService.registerPaymentSubmittedToProviderEventFor(transaction);
    }

    public DirectDebitEvent paymentFailedWithEmailFor(Transaction transaction) {
        userNotificationService.sendPaymentFailedEmailFor(transaction);
        return paymentFailedFor(transaction);
    }

    public DirectDebitEvent paymentFailedWithoutEmailFor(Transaction transaction) {
        return paymentFailedFor(transaction);
    }

    private DirectDebitEvent paymentFailedFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_FAILED);
        return directDebitEventService.registerPaymentFailedEventFor(updatedTransaction);
    }

    public DirectDebitEvent paymentPaidOutFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAID_OUT);
        return directDebitEventService.registerPaymentPaidOutEventFor(updatedTransaction);
    }

    public DirectDebitEvent paymentAcknowledgedFor(Transaction transaction) {
        return directDebitEventService.registerPaymentAcknowledgedEventFor(transaction);
    }

    public DirectDebitEvent paymentCancelledFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        return directDebitEventService
                .registerPaymentCancelledEventFor(transaction.getMandate(), newTransaction);
    }

    public DirectDebitEvent paymentMethodChangedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        return directDebitEventService.registerPaymentMethodChangedEventFor(newTransaction.getMandate());
    }

    public DirectDebitEvent paymentSubmittedFor(Transaction transaction) {
        return directDebitEventService.registerPaymentSubmittedEventFor(transaction);
    }

    public DirectDebitEvent payoutPaidFor(Transaction transaction) {
        return directDebitEventService.registerPayoutPaidEventFor(transaction);
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

    public Optional<DirectDebitEvent> findPaymentSubmittedEventFor(Transaction transaction) {
        return directDebitEventService.findBy(transaction.getId(), CHARGE, PAYMENT_SUBMITTED_TO_BANK);
    }
}
