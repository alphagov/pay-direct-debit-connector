package uk.gov.pay.directdebit.partnerapp.services;

import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.api.GoCardlessAppConnectStateResponse;
import uk.gov.pay.directdebit.partnerapp.client.GoCardlessAppConnectClient;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessAppConnectAccessTokenResponse;
import uk.gov.pay.directdebit.partnerapp.dao.GoCardlessAppConnectAccountTokenDao;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectCodeExchangeErrorResponse;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.URI;

public class GoCardlessAppConnectAccountService {

    private final GoCardlessAppConnectAccountTokenDao goCardlessAppConnectAccountTokenDao;
    private final GatewayAccountDao gatewayAccountDao;
    private final GoCardlessAppConnectClient connectClient;

    @Inject
    public GoCardlessAppConnectAccountService(GoCardlessAppConnectAccountTokenDao goCardlessAppConnectAccountTokenDao,
                                              GatewayAccountDao gatewayAccountDao,
                                              GoCardlessAppConnectClient connectClient) {
        this.goCardlessAppConnectAccountTokenDao = goCardlessAppConnectAccountTokenDao;
        this.gatewayAccountDao = gatewayAccountDao;
        this.connectClient = connectClient;
    }

    public Response createToken(String gatewayAccountExternalId,
                                String redirectUri) {

        return gatewayAccountDao.findByExternalId(gatewayAccountExternalId)
                .map(account -> goCardlessAppConnectAccountTokenDao.findByGatewayAccountId(account.getId())
                        .map(token -> {
                            goCardlessAppConnectAccountTokenDao.disableToken(token.getToken(), account.getId());
                            return mapEntity(insertToken(account, redirectUri));
                        })
                        .orElseGet(() -> mapEntity(insertToken(account, redirectUri))))
                .orElseThrow(() -> new BadRequestException("There is no gateway account with external id " + gatewayAccountExternalId));
    }

    public Response exchangeCodeForToken(String accessCode, String partnerToken) {
        return goCardlessAppConnectAccountTokenDao.findActiveTokenByToken(partnerToken)
                .map(token -> gatewayAccountDao.findById(token.getGatewayAccountId())
                        .map(gatewayAccount -> connectClient.postAccessCode(accessCode, gatewayAccount, token.getRedirectUri())
                                .map(clientResponse -> processGoCardlessResponse(clientResponse, gatewayAccount))
                                .orElseThrow(() -> new InternalServerErrorException("There is no response from GoCardless Connect client")))
                        .orElseThrow(() -> new BadRequestException("There is no gateway account with id " + token.getGatewayAccountId().toString())))
                .orElseThrow(() -> new BadRequestException("There is no token with value " + partnerToken));
    }

    private Response processGoCardlessResponse(GoCardlessAppConnectAccessTokenResponse response, GatewayAccount gatewayAccount) {
        if (GoCardlessAppConnectClient.isValidResponse(response)) {
            gatewayAccountDao.updateAccessTokenAndOrganisation(gatewayAccount.getExternalId(), response.getAccessToken(), response.getOrganisationId());
            return Response.ok().build();
        } else if (StringUtils.isNotBlank(response.getError())) {
            return Response.status(500).entity(GoCardlessAppConnectCodeExchangeErrorResponse.from(response)).build();
        } else {
            throw new BadRequestException("Received and invalid response from GoCardless Connect");
        }
    }

    private GoCardlessAppConnectAccountEntity insertToken(GatewayAccount account, String redirectUri) {
        GoCardlessAppConnectAccountEntity newToken = new GoCardlessAppConnectAccountEntity();
        newToken.setGatewayAccountId(account.getId());
        newToken.setToken(RandomIdGenerator.newId());
        newToken.setActive(Boolean.TRUE);
        newToken.setRedirectUri(redirectUri);
        Long newId = goCardlessAppConnectAccountTokenDao.insert(newToken);
        newToken.setId(newId);
        return newToken;
    }

    private Response mapEntity(GoCardlessAppConnectAccountEntity newEntity) {
        GoCardlessAppConnectStateResponse response = GoCardlessAppConnectStateResponse.from(newEntity);
        URI location = URI.create("/v1/api/gocardless/partnerapp/tokens/" + response.getToken());
        return Response.created(location).entity(response).build();
    }
}
