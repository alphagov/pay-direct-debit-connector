package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.SandboxPaymentStatesGraph.getStates;

public class TransactionService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(TransactionService.class);
    private final TransactionDao transactionDao;
    private final PaymentRequestEventService paymentRequestEventService;

    @Inject
    public TransactionService(TransactionDao transactionDao, PaymentRequestEventService paymentRequestEventService) {
        this.paymentRequestEventService = paymentRequestEventService;
        this.transactionDao = transactionDao;
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
                SandboxPaymentStatesGraph.initialState());
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
                    Transaction newCharge = updateStateFor(charge, SupportedEvent.TOKEN_EXCHANGED);
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
        return updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public Transaction confirmedDirectDebitDetailsFor(Long accountId, String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalIdAndGatewayAccountId(paymentRequestExternalId, accountId);
        paymentRequestEventService.registerDirectDebitConfirmedEventFor(transaction);
        return updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public Transaction payerCreatedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.PAYER_CREATED);
        paymentRequestEventService.registerPayerCreatedEventFor(transaction);
        return newTransaction;
    }

    public PaymentRequestEvent mandateCreatedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.MANDATE_CREATED);
        return paymentRequestEventService.registerMandateCreatedEventFor(newTransaction);
    }

    public PaymentRequestEvent paidOutFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.PAID_OUT);
        return paymentRequestEventService.registerPaidOutEventFor(newTransaction);
    }

    private Transaction updateStateFor(Transaction transaction, SupportedEvent event) {
        PaymentState newState = getStates().getNextStateForEvent(transaction.getState(),
                event);
        transactionDao.updateState(transaction.getId(), newState);
        transaction.setState(newState);
        LOGGER.info("Updated transaction {} - from {} to {}",
                transaction.getPaymentRequestExternalId(),
                transaction.getState(),
                newState);
        return transaction;
    }
}
