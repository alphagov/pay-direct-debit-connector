package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.exception.InvalidStateException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.CHARGE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type.MANDATE;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;

public class TransactionService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(TransactionService.class);
    private final TransactionDao transactionDao;
    private final PaymentRequestEventService paymentRequestEventService;
    private final UserNotificationService userNotificationService;
    @Inject
    public TransactionService(TransactionDao transactionDao, PaymentRequestEventService paymentRequestEventService, UserNotificationService userNotificationService) {
        this.paymentRequestEventService = paymentRequestEventService;
        this.transactionDao = transactionDao;
        this.userNotificationService = userNotificationService;
    }

    public Transaction findChargeForExternalIdAndGatewayAccountId(String paymentRequestExternalId, Long accountId) {
        Transaction transaction = transactionDao.findByPaymentRequestExternalIdAndAccountId(paymentRequestExternalId, accountId)
                .orElseThrow(() -> new ChargeNotFoundException(paymentRequestExternalId));
        LOGGER.info("Found charge for payment request with id: {} for gateway account id: {}", paymentRequestExternalId, accountId);
        return transaction;
    }

    Transaction createChargeFor(PaymentRequest paymentRequest, GatewayAccount gatewayAccount) {
        Transaction transaction = new Transaction(
                paymentRequest.getId(),
                paymentRequest.getExternalId(),
                paymentRequest.getDescription(),
                paymentRequest.getGatewayAccountId(),
                gatewayAccount.getPaymentProvider(),
                paymentRequest.getReturnUrl(),
                paymentRequest.getAmount(),
                Transaction.Type.CHARGE,
                PaymentStatesGraph.initialState());
        LOGGER.info("Created transaction for payment request {}", paymentRequest.getExternalId());
        Long id = transactionDao.insert(transaction);
        transaction.setId(id);
        paymentRequestEventService.insertEventFor(paymentRequest, chargeCreated(paymentRequest.getId()));
        return transaction;
    }

    public List<Transaction> findAllByPaymentStateAndProvider(PaymentState paymentState, PaymentProvider paymentProvider) {
        return transactionDao.findAllByPaymentStateAndProvider(paymentState, paymentProvider);
    }


    public Optional<Transaction> findTransactionForToken(String token) {
        return transactionDao
                .findByTokenId(token).map(charge -> {
                    Transaction newCharge = updateStateFor(charge, TOKEN_EXCHANGED);
                    paymentRequestEventService.registerTokenExchangedEventFor(newCharge);
                    return newCharge;
                });
    }

    public Transaction findTransactionFor(Long transactionId) {
        return transactionDao
                .findById(transactionId)
                .orElseThrow(() -> new ChargeNotFoundException(transactionId.toString()));
    }

    public Transaction findTransactionForMandateId(Long mandateId) {
        Transaction transaction = transactionDao.findByMandateId(mandateId)
                .orElseThrow(() -> new ChargeNotFoundException(mandateId.toString()));
        LOGGER.info("Found transaction {} for mandate {}", transaction.getId(), mandateId.toString());
        return transaction;
    }

    public Transaction receiveDirectDebitDetailsFor(Long accountId, String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalIdAndGatewayAccountId(paymentRequestExternalId, accountId);
        paymentRequestEventService.registerDirectDebitReceivedEventFor(transaction);
        return updateStateFor(transaction, DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public Transaction confirmedDirectDebitDetailsFor(Long accountId, String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalIdAndGatewayAccountId(paymentRequestExternalId, accountId);
        paymentRequestEventService.registerDirectDebitConfirmedEventFor(transaction);
        return updateStateFor(transaction, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public Transaction payerCreatedFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAYER_CREATED);
        paymentRequestEventService.registerPayerCreatedEventFor(transaction);
        return updatedTransaction;
    }

    public PaymentRequestEvent paymentCreatedFor(Transaction transaction) {
        transactionDao.findById(transaction.getId());
        Transaction updatedTransaction = updateStateFor(transaction, PAYMENT_CREATED);
        return paymentRequestEventService.registerPaymentCreatedEventFor(updatedTransaction);
    }

    public PaymentRequestEvent mandateFailedFor(Transaction transaction, Payer payer) {
        userNotificationService.sendMandateFailedEmail(payer, transaction);
        return paymentRequestEventService.registerMandateFailedEventFor(transaction);
    }

    public PaymentRequestEvent paymentPendingFor(Transaction transaction) {
        return paymentRequestEventService.registerPaymentPendingEventFor(transaction);
    }

    public PaymentRequestEvent paymentFailedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_FAILED);
        return paymentRequestEventService.registerPaymentFailedEventFor(newTransaction);
    }

    public PaymentRequestEvent paymentPaidOutFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAID_OUT);
        return paymentRequestEventService.registerPaymentPaidOutEventFor(updatedTransaction);
    }

    public PaymentRequestEvent mandatePendingFor(Transaction transaction) {
        return paymentRequestEventService.registerMandatePendingEventFor(transaction);
    }

    public PaymentRequestEvent mandateActiveFor(Transaction transaction) {
        return paymentRequestEventService.registerMandateActiveEventFor(transaction);
    }

    private Transaction updateStateFor(Transaction transaction, SupportedEvent event) {
        PaymentState newState = getStates().getNextStateForEvent(transaction.getState(),
                event);
        transactionDao.updateState(transaction.getId(), newState);
        LOGGER.info("Updating transaction {} - from {} to {}",
                transaction.getPaymentRequestExternalId(),
                transaction.getState(),
                newState);
        transaction.setState(newState);
        return transaction;
    }

    public PaymentRequestEvent findPaymentPendingEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getPaymentRequestId(), CHARGE, PAYMENT_PENDING)
                .orElseThrow(() -> new InvalidStateException(format("Could not find payment pending event for payment request with id: {}",
                        transaction.getPaymentRequestExternalId())));
    }

    public Optional<PaymentRequestEvent> findMandatePendingEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getPaymentRequestId(), MANDATE, MANDATE_PENDING);
    }
}
