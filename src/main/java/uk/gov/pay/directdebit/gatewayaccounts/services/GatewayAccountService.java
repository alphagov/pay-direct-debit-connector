package uk.gov.pay.directdebit.gatewayaccounts.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.resources.GatewayAccountResource;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class GatewayAccountService {

    GatewayAccountDao gatewayAccountDao;
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountService.class);

    public GatewayAccountService(GatewayAccountDao gatewayAccountDao) {
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public GatewayAccount getGatewayAccount(Long accountId) {
        return gatewayAccountDao
                .findById(accountId)
                .orElseThrow(() -> new GatewayAccountNotFoundException(accountId));
    }

    public List<GatewayAccount> getAllGatewayAccounts() {
        return gatewayAccountDao.findAll();
    }

    public Payer create(GatewayAccount gatewayAccount) {
        Long id = gatewayAccountDao.insert(gatewayAccount);
        gatewayAccount.setId(id);
        LOGGER.info("Created Gateway Account with id {}", id);
        return gatewayAccount;
    }
}
