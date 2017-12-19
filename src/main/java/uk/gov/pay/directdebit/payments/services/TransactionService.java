package uk.gov.pay.directdebit.payments.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Optional;

import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.chargeCreated;
import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;

public class TransactionService {
    private static final Logger logger = PayLoggerFactory.getLogger(TransactionService.class);

    private final TransactionDao transactionDao;
    private final PaymentRequestEventService paymentRequestEventService;
    public TransactionService(TransactionDao transactionDao, PaymentRequestEventService paymentRequestEventService) {
        this.paymentRequestEventService = paymentRequestEventService;
        this.transactionDao = transactionDao;
    }

    public Transaction findChargeForExternalId(String paymentRequestExternalId) {
        Transaction transaction = transactionDao.findByPaymentRequestExternalId(paymentRequestExternalId)
                .orElseThrow(() -> new ChargeNotFoundException(paymentRequestExternalId));
        logger.info("Found charge for payment request with id: {}", paymentRequestExternalId);
        return transaction;
    }

    Transaction createChargeFor(PaymentRequest paymentRequest){
        Transaction transaction = new Transaction(
                paymentRequest.getId(),
                paymentRequest.getExternalId(),
                paymentRequest.getAmount(),
                Transaction.Type.CHARGE,
                PaymentStatesGraph.initialState());
        logger.info("Created transaction for payment request {}", paymentRequest.getExternalId());
        Long id = transactionDao.insert(transaction);
        transaction.setId(id);
        paymentRequestEventService.insertEventFor(paymentRequest, chargeCreated(paymentRequest.getId()));
        return transaction;
    }

    private Transaction updateStateFor(Transaction charge, PaymentRequestEvent.SupportedEvent event){
        PaymentState newState = getStates().getNextStateForEvent(charge.getState(),
                event);
        transactionDao.updateState(charge.getId(), newState);
        charge.setState(newState);
        logger.info("Updated charge {} - from {} to {}",
                charge.getPaymentRequestExternalId(),
                charge.getState(),
                newState);
        return charge;
    }

    public Optional<Transaction> findChargeForToken(String token) {
        return transactionDao
                .findByTokenId(token).map(c -> {
                    Transaction newCharge = updateStateFor(c, PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED);
                    paymentRequestEventService.registerTokenExchangedEventFor(newCharge);
                    return newCharge;
                });
    }

    public Transaction receiveDirectDebitDetailsFor(String paymentRequestExternalId) {
        Transaction transaction = findChargeForExternalId(paymentRequestExternalId);
        paymentRequestEventService.registerDirectDebitReceivedEventFor(transaction);
        return updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_RECEIVED);
    }

    public Transaction payerCreatedFor(Transaction transaction) {
        Transaction newTransaction = updateStateFor(transaction, PaymentRequestEvent.SupportedEvent.PAYER_CREATED);
        paymentRequestEventService.registerPayerCreatedEventFor(transaction);
        return newTransaction;
    }
}
