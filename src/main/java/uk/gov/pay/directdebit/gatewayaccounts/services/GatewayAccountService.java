package uk.gov.pay.directdebit.gatewayaccounts.services;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class GatewayAccountService {

    private GatewayAccountDao gatewayAccountDao;
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountService.class);

    private GatewayAccountParser gatewayAccountParser;

    @Inject
    public GatewayAccountService(GatewayAccountDao gatewayAccountDao, GatewayAccountParser gatewayAccountParser) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.gatewayAccountParser = gatewayAccountParser;
    }

    public GatewayAccount getGatewayAccountForId(String accountExternalId) {
        return gatewayAccountDao
                .findByExternalId(accountExternalId)
                .orElseThrow(() -> new GatewayAccountNotFoundException(accountExternalId));
    }

    public GatewayAccount getGatewayAccountFor(Transaction transaction) {
        return gatewayAccountDao
                .findById(transaction.getMandate().getGatewayAccount().getId())
                .orElseThrow(() -> new GatewayAccountNotFoundException(transaction.getMandate().getGatewayAccount().getId().toString()));
    }

    public List<GatewayAccount> getAllGatewayAccounts() {
        return gatewayAccountDao.findAll();
    }

    public GatewayAccount create(Map<String, String> createGatewayAccountRequest) {
        GatewayAccount gatewayAccount = gatewayAccountParser.parse(createGatewayAccountRequest);
        Long id = gatewayAccountDao.insert(gatewayAccount);
        gatewayAccount.setId(id);
        LOGGER.info("Created Gateway Account with id {}", id);
        return gatewayAccount;
    }
}
