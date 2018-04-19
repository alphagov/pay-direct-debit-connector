package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAID_OUT;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYER_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_CREATED;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.PAYMENT_SUBMITTED;
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

    public Transaction findTransactionForExternalIdAndGatewayAccountExternalId(String paymentRequestExternalId, String accountExternalId) {
        Transaction transaction = transactionDao.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId)
                .orElseThrow(() -> new ChargeNotFoundException("payment request external id", paymentRequestExternalId));
        LOGGER.info("Found charge for payment request with id: {} for gateway account id: {}", paymentRequestExternalId, accountExternalId);
        return transaction;
    }

    Transaction createChargeFor(PaymentRequest paymentRequest, GatewayAccount gatewayAccount) {
        Transaction transaction = new Transaction(
                null,
                paymentRequest.getId(),
                paymentRequest.getExternalId(),
                paymentRequest.getDescription(),
                paymentRequest.getReference(),
                gatewayAccount.getId(),
                gatewayAccount.getExternalId(),
                gatewayAccount.getPaymentProvider(),
                paymentRequest.getReturnUrl(),
                paymentRequest.getAmount(),
                Transaction.Type.CHARGE,
                PaymentStatesGraph.initialState()
        );
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
                .orElseThrow(() -> new ChargeNotFoundException("transaction id", transactionId.toString()));
    }

    public Transaction findTransactionForMandateId(Long mandateId) {
        Transaction transaction = transactionDao.findByMandateId(mandateId)
                .orElseThrow(() -> new ChargeNotFoundException("mandate id", mandateId.toString()));
        LOGGER.info("Found transaction {} for mandate {}", transaction.getId(), mandateId.toString());
        return transaction;
    }

    public Transaction receiveDirectDebitDetailsFor(String accountExternalId, String paymentRequestExternalId) {
        Transaction transaction = findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId);
        paymentRequestEventService.registerDirectDebitReceivedEventFor(transaction);
        return updateStateFor(transaction, DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public Transaction confirmedDirectDebitDetailsFor(String accountExternalId, String paymentRequestExternalId) {
        Transaction transaction = findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequestExternalId, accountExternalId);
        paymentRequestEventService.registerDirectDebitConfirmedEventFor(transaction);
        return updateStateFor(transaction, DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public Transaction payerCreatedFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAYER_CREATED);
        paymentRequestEventService.registerPayerCreatedEventFor(transaction);
        return updatedTransaction;
    }

    public PaymentRequestEvent paymentCreatedFor(Transaction transaction, Payer payer, LocalDate earliestChargeDate) {
        userNotificationService.sendPaymentConfirmedEmailFor(transaction, payer, earliestChargeDate);
        Transaction updatedTransaction = updateStateFor(transaction, PAYMENT_CREATED);
        return paymentRequestEventService.registerPaymentCreatedEventFor(updatedTransaction);
    }

    public PaymentRequestEvent paymentFailedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_FAILED);
        return paymentRequestEventService.registerPaymentFailedEventFor(newTransaction);
    }

    public PaymentRequestEvent paymentPaidOutFor(Transaction transaction) {
        Transaction updatedTransaction = updateStateFor(transaction, PAID_OUT);
        return paymentRequestEventService.registerPaymentPaidOutEventFor(updatedTransaction);
    }

    public PaymentRequestEvent paymentPendingFor(Transaction transaction) {
        return paymentRequestEventService.registerPaymentPendingEventFor(transaction);
    }

    public PaymentRequestEvent paymentCancelledFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER);
        return paymentRequestEventService.registerPaymentCancelledEventFor(newTransaction);
    }

    public PaymentRequestEvent paymentSubmittedFor(Transaction transaction) {
        return paymentRequestEventService.registerPaymentSubmittedEventFor(transaction);
    }

    public PaymentRequestEvent payoutPaidFor(Transaction transaction) {
        return paymentRequestEventService.registerPayoutPaidEventFor(transaction);
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

    public Optional<PaymentRequestEvent> findPaymentSubmittedEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getPaymentRequestId(), CHARGE, PAYMENT_SUBMITTED);
    }

    public Optional<PaymentRequestEvent> findMandatePendingEventFor(Transaction transaction) {
        return paymentRequestEventService.findBy(transaction.getPaymentRequestId(), MANDATE, MANDATE_PENDING);
    }

    public PaymentRequestEvent paymentMethodChangedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, SupportedEvent.PAYMENT_CANCELLED_BY_USER_NOT_ELIGIBLE);
        return paymentRequestEventService.registerPaymentMethodChangedEventFor(newTransaction);
    }
}
