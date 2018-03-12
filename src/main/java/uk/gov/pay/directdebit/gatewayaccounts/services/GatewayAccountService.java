package uk.gov.pay.directdebit.gatewayaccounts.services;

import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class GatewayAccountService {

    private GatewayAccountDao gatewayAccountDao;
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountService.class);

    private GatewayAccountParser gatewayAccountParser;

    @Inject
    public GatewayAccountService(GatewayAccountDao gatewayAccountDao, GatewayAccountParser gatewayAccountParser) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.gatewayAccountParser = gatewayAccountParser;
    }

    public GatewayAccount getGatewayAccount(String accountExternalId) {
        return gatewayAccountDao
                .findByExternalId(accountExternalId)
                .orElseThrow(() -> new GatewayAccountNotFoundException(accountExternalId));
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
