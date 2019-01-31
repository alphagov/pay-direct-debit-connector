package uk.gov.pay.directdebit.gatewayaccounts.services;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.api.PatchGatewayAccountValidator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GatewayAccountService {

    private GatewayAccountDao gatewayAccountDao;

    private static final Splitter COMMA_SEPARATOR = Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayAccountService.class);

    private GatewayAccountParser gatewayAccountParser;
    private PatchGatewayAccountValidator validator = new PatchGatewayAccountValidator();

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

    GatewayAccount getGatewayAccountFor(Transaction transaction) {
        return gatewayAccountDao
                .findById(transaction.getMandate().getGatewayAccount().getId())
                .orElseThrow(() -> new GatewayAccountNotFoundException(transaction.getMandate().getGatewayAccount().getId().toString()));
    }

    public List<GatewayAccountResponse> getAllGatewayAccounts(String externalAccountIdsArg, UriInfo uriInfo) {
        List<String> externalAccountIds = COMMA_SEPARATOR.splitToList(externalAccountIdsArg);

        return (
                externalAccountIds.isEmpty()
                        ? gatewayAccountDao.findAll()
                        : gatewayAccountDao.find(externalAccountIds)
        )
                .stream()
                .map(gatewayAccount -> GatewayAccountResponse.from(gatewayAccount).withSelfLink(uriInfo))
                .collect(Collectors.toList());
    }

    public GatewayAccount create(Map<String, String> createGatewayAccountRequest) {
        GatewayAccount gatewayAccount = gatewayAccountParser.parse(createGatewayAccountRequest);
        Long id = gatewayAccountDao.insert(gatewayAccount);
        gatewayAccount.setId(id);
        LOGGER.info("Created Gateway Account with id {}", id);
        return gatewayAccount;
    }
    
    public GatewayAccount patch(String externalId, List<Map<String, String>> request) {
        validator.validatePatchRequest(externalId, request);
        PaymentProviderAccessToken accessToken = null;
        PaymentProviderOrganisationIdentifier organisation = null;
        for (Map<String, String> operation : request) {
            if (operation.get("path").equals("access_token")) {
                accessToken = PaymentProviderAccessToken.of(operation.get("value"));
            } else {
                organisation = PaymentProviderOrganisationIdentifier.of(operation.get("value"));
            }
        }
        
        gatewayAccountDao.updateAccessTokenAndOrganisation(externalId, accessToken, organisation);
        
        return this.getGatewayAccountForId(externalId);
    }
}
