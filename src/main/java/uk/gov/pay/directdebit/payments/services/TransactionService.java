package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.SandboxPaymentStatesGraph.getStates;

public class TransactionService {

    private static final Logger LOGGER = PayLoggerFactory.getLogger(TransactionService.class);
    private final TransactionDao transactionDao;
    private final PaymentRequestEventService paymentRequestEventService;

    public TransactionService(TransactionDao transactionDao, PaymentRequestEventService paymentRequestEventService) {
        this.paymentRequestEventService = paymentRequestEventService;
        this.transactionDao = transactionDao;
    }

    Transaction findChargeForExternalId(String paymentRequestExternalId) {
        Transaction transaction = transactionDao.findByPaymentRequestExternalId(paymentRequestExternalId)
                .orElseThrow(() -> new ChargeNotFoundException(paymentRequestExternalId));
        LOGGER.info("Found charge for payment request with id: {}", paymentRequestExternalId);
        return transaction;
    }

    Transaction createChargeFor(PaymentRequest paymentRequest) {
        Transaction transaction = new Transaction(
                paymentRequest.getId(),
                paymentRequest.getExternalId(),
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

    public Optional<Transaction> findChargeForToken(String token) {
        return transactionDao
                .findByTokenId(token).map(charge -> {
                    Transaction newCharge = updateStateFor(charge, SupportedEvent.TOKEN_EXCHANGED);
                    paymentRequestEventService.registerTokenExchangedEventFor(newCharge);
                    return newCharge;
                });
    }

    public Transaction receiveDirectDebitDetailsFor(String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalId(paymentRequestExternalId);
        paymentRequestEventService.registerDirectDebitReceivedEventFor(transaction);
        return updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public Transaction confirmedDirectDebitDetailsFor(String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalId(paymentRequestExternalId);
        paymentRequestEventService.registerDirectDebitConfirmedEventFor(transaction);
        return updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED);
    }

    public Transaction payerCreatedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.PAYER_CREATED);
        paymentRequestEventService.registerPayerCreatedEventFor(transaction);
        return newTransaction;
    }

    public Transaction mandateCreatedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.MANDATE_CREATED);
        paymentRequestEventService.registerMandateCreatedEventFor(transaction);
        return newTransaction;
    }

    private Transaction updateStateFor(Transaction charge, SupportedEvent event) {
        PaymentState newState = getStates().getNextStateForEvent(charge.getState(),
                event);
        transactionDao.updateState(charge.getId(), newState);
        charge.setState(newState);
        LOGGER.info("Updated charge {} - from {} to {}",
                charge.getPaymentRequestExternalId(),
                charge.getState(),
                newState);
        return charge;
    }
}
