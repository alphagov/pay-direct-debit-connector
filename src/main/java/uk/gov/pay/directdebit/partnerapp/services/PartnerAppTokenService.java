package uk.gov.pay.directdebit.partnerapp.services;

import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.client.GoCardlessConnectClient;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectClientResponse;
import uk.gov.pay.directdebit.partnerapp.dao.PartnerAppTokenDao;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppCodeExchangeErrorResponse;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class PartnerAppTokenService {

    private final PartnerAppTokenDao partnerAppTokenDao;
    private final GatewayAccountDao gatewayAccountDao;
    private final GoCardlessConnectClient connectClient;

    @Inject
    public PartnerAppTokenService(PartnerAppTokenDao partnerAppTokenDao,
                                  GatewayAccountDao gatewayAccountDao,
                                  GoCardlessConnectClient connectClient) {
        this.partnerAppTokenDao = partnerAppTokenDao;
        this.gatewayAccountDao = gatewayAccountDao;
        this.connectClient = connectClient;
    }

    public Optional<PartnerAppTokenEntity> createToken(String gatewayAccountExternalId,
                                                       String redirectUri) {

        return gatewayAccountDao.findByExternalId(gatewayAccountExternalId)
                .map(account -> partnerAppTokenDao.findByGatewayAccountId(account.getId())
                        .map(token -> {
                            partnerAppTokenDao.disableToken(token.getToken(), account.getId());
                            return insertToken(account, redirectUri);
                        }).orElseGet(() -> insertToken(account, redirectUri)));
    }

    public Response exchangeCodeForToken(String accessCode, String partnerToken) {
        Optional<PartnerAppTokenEntity> token = partnerAppTokenDao.findActiveTokenByToken(partnerToken);
        if (token.isPresent()) {
            Optional<GatewayAccount> gatewayAccountDaoById = gatewayAccountDao.findById(token.get().getGatewayAccountId());
            if (gatewayAccountDaoById.isPresent()) {
                Optional<GoCardlessConnectClientResponse> clientResponse = connectClient.postAccessCode(accessCode, gatewayAccountDaoById.get(), token.get().getRedirectUri());
                if (clientResponse.isPresent()) {
                    return processGoCardlessResponse(clientResponse.get(), gatewayAccountDaoById.get());
                } else {
                    throw new BadRequestException("There is no response from GoCardless Connect client");
                }
            } else {
                throw new BadRequestException("There is no gateway account with id " + token.get().getGatewayAccountId().toString());
            }
        } else {
            throw new BadRequestException("There is no token with value " + partnerToken);
        }
    }

    private Response processGoCardlessResponse(GoCardlessConnectClientResponse response, GatewayAccount gatewayAccount) {
        if (GoCardlessConnectClient.isValidResponse(response)) {
            gatewayAccountDao.updateAccessTokenAndOrganisation(gatewayAccount.getExternalId(), response.getAccessToken(), response.getOrganisationId());
            return Response.ok().build();
        } else if (StringUtils.isNotBlank(response.getError())) {
            return Response.status(400).entity(PartnerAppCodeExchangeErrorResponse.from(response)).build();
        } else {
            throw new BadRequestException("Received and invalid response from GoCardless Connect");
        }
    }

    private PartnerAppTokenEntity insertToken(GatewayAccount account, String redirectUri) {
        PartnerAppTokenEntity newToken = new PartnerAppTokenEntity();
        newToken.setGatewayAccountId(account.getId());
        newToken.setToken(RandomIdGenerator.newId());
        newToken.setActive(Boolean.TRUE);
        newToken.setRedirectUri(redirectUri);
        Long newId = partnerAppTokenDao.insert(newToken);
        newToken.setId(newId);
        return newToken;
    }
}
