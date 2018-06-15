package uk.gov.pay.directdebit.tokens.services;

import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;

public class TokenService {
    private static final Logger LOGGER = PayLoggerFactory.getLogger(TokenService.class);

    private final TokenDao tokenDao;

    @Inject
    public TokenService(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    public Token generateNewTokenFor(Mandate mandate) {
        Token token = Token.generateNewTokenFor(mandate.getId());
        LOGGER.info("Generating new one-time token for mandate {}", mandate.getExternalId());
        Long id = tokenDao.insert(token);
        token.setId(id);
        return token;
    }
    
    public void deleteToken(String token) {
        int numOfDeletedTokens = tokenDao.deleteToken(token);
        if (numOfDeletedTokens == 0) {
            LOGGER.warn("Tried to delete token which was not in db");
        }
    }

}
