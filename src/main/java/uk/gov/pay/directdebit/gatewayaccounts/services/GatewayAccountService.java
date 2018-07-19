package uk.gov.pay.directdebit.gatewayaccounts.services;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import uk.gov.pay.directdebit.app.logger.PayLoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountCommandDao;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountSelectDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GatewayAccountService {

    private GatewayAccountCommandDao gatewayAccountCommandDao;
    private GatewayAccountSelectDao gatewayAccountSelectDao;

    private static final Splitter COMMA_SEPARATOR = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Logger LOGGER = PayLoggerFactory.getLogger(GatewayAccountService.class);

    private GatewayAccountParser gatewayAccountParser;

    @Inject
    public GatewayAccountService(GatewayAccountSelectDao gatewayAccountSelectDao, 
                                 GatewayAccountCommandDao gatewayAccountCommandDao,
                                 GatewayAccountParser gatewayAccountParser) {
        this.gatewayAccountSelectDao = gatewayAccountSelectDao;
        this.gatewayAccountCommandDao = gatewayAccountCommandDao;
        this.gatewayAccountParser = gatewayAccountParser;
    }

    public GatewayAccount getGatewayAccountForId(String accountExternalId) {
        return gatewayAccountSelectDao
                .findByExternalId(accountExternalId)
                .orElseThrow(() -> new GatewayAccountNotFoundException(accountExternalId));
    }

    GatewayAccount getGatewayAccountFor(Transaction transaction) {
        return gatewayAccountSelectDao
                .findById(transaction.getMandate().getGatewayAccount().getId())
                .orElseThrow(() -> new GatewayAccountNotFoundException(transaction.getMandate().getGatewayAccount().getId().toString()));
    }

    public List<GatewayAccountResponse> getAllGatewayAccounts(String externalAccountIdsArg, UriInfo uriInfo) {
        List<String> externalAccountIds = COMMA_SEPARATOR.splitToList(externalAccountIdsArg);

        List<GatewayAccountResponse> gatewayAccounts = (
                externalAccountIds.isEmpty()
                        ? gatewayAccountSelectDao.findAll()
                        : gatewayAccountSelectDao.find(externalAccountIds)
        )
                .stream()
                .map(gatewayAccount -> GatewayAccountResponse.from(gatewayAccount).withSelfLink(uriInfo))
                .collect(Collectors.toList());

        return gatewayAccounts;
    }

    public GatewayAccount create(Map<String, String> createGatewayAccountRequest) {
        GatewayAccount gatewayAccount = gatewayAccountParser.parse(createGatewayAccountRequest);
        Optional<Long> id = gatewayAccountCommandDao.insert(gatewayAccount);
        gatewayAccount.setId(id.get());
        LOGGER.info("Created Gateway Account with id {}", id);
        return gatewayAccount;
    }
}
