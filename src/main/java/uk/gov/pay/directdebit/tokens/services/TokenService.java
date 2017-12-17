package uk.gov.pay.directdebit.tokens.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;

public class TokenService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(TokenService.class);

    private final TokenDao tokenDao;
    private final TransactionService transactionService;

    public TokenService(TokenDao tokenDao, TransactionService transactionService) {
        this.tokenDao = tokenDao;
        this.transactionService = transactionService;
    }

    public Token generateNewTokenFor(PaymentRequest paymentRequest) {
        Token token = Token.generateNewTokenFor(paymentRequest.getId());
        LOGGER.info("Generating new one-time token for payment request {}", paymentRequest.getExternalId());
        Long id = tokenDao.insert(token);
        token.setId(id);
        return token;
    }

    public Transaction validateChargeWithToken(String token) {
        return transactionService
                .findChargeForToken(token)
                .orElseThrow(TokenNotFoundException::new);
    }

    public void deleteToken(String token) {
        int numOfDeletedTokens = tokenDao.deleteToken(token);
        if (numOfDeletedTokens == 0) {
            LOGGER.warn("Tried to delete token which was not in db");
        }
    }

}
