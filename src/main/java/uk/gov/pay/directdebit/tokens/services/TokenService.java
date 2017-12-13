package uk.gov.pay.directdebit.tokens.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;

import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.model.PaymentStatesGraph.getStates;

public class TokenService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(TokenService.class);

    private final TokenDao tokenDao;
    private final TransactionDao transactionDao;
    private final PaymentRequestEventDao paymentRequestEventDao;

    public TokenService(TokenDao tokenDao, PaymentRequestEventDao paymentRequestEventDao, TransactionDao transactionDao) {
        this.tokenDao = tokenDao;
        this.paymentRequestEventDao = paymentRequestEventDao;
        this.transactionDao = transactionDao;
    }

    public Transaction validateChargeWithToken(String token) {
        return transactionDao
                .findByTokenId(token)
                .map(charge -> {
                    LOGGER.info("Found one-time token for charge {}", charge.getPaymentRequestExternalId());
                    updateChargeState(charge);
                    insertTokenExchangedEventFor(charge);
                    return charge;
                })
                .orElseThrow(TokenNotFoundException::new);
    }

    public void deleteToken(String token){
        int numOfDeletedTokens = tokenDao.deleteToken(token);
        if (numOfDeletedTokens == 0) {
            LOGGER.warn("Tried to delete token which was not in db");
        }
    }

    private void insertTokenExchangedEventFor(Transaction charge) {
        PaymentRequestEvent paymentRequestEvent = new PaymentRequestEvent(
                charge.getPaymentRequestId(),
                PaymentRequestEvent.Type.CHARGE,
                PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED,
                ZonedDateTime.now());
        LOGGER.info("Token Exchanged event for payment request {}", charge.getPaymentRequestId());
        paymentRequestEventDao.insert(paymentRequestEvent);
    }

    private void updateChargeState(Transaction charge) {
        PaymentState newState = getStates().getNextStateForEvent(charge.getState(),
                PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED);
        transactionDao.updateState(charge.getId(), newState);
        charge.setState(newState);
        LOGGER.info("Updated charge {} - from {} to {}",
                charge.getPaymentRequestId(),
                charge.getState(),
                newState);
    }
}
