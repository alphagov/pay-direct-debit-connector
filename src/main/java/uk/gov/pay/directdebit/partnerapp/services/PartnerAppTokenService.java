package uk.gov.pay.directdebit.partnerapp.services;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.dao.PartnerAppTokenDao;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;

import javax.inject.Inject;
import java.util.Optional;

public class PartnerAppTokenService {

    private final PartnerAppTokenDao partnerAppTokenDao;
    private final GatewayAccountDao gatewayAccountDao;

    @Inject
    public PartnerAppTokenService(PartnerAppTokenDao partnerAppTokenDao,
                                  GatewayAccountDao gatewayAccountDao) {
        this.partnerAppTokenDao = partnerAppTokenDao;
        this.gatewayAccountDao = gatewayAccountDao;
    }

    public Optional<PartnerAppTokenEntity> createToken(String gatewayAccountExternalId) {

        return gatewayAccountDao.findByExternalId(gatewayAccountExternalId)
                .map(account -> partnerAppTokenDao.findByGatewayAccountId(account.getId())
                        .map(token -> {
                            partnerAppTokenDao.disableToken(token.getToken(), account.getId());
                            return insertToken(account);
                        }).orElseGet(() -> insertToken(account)));
    }

    public Optional<Integer> disableToken(String token, String gatewayAccountExternalId) {
        return gatewayAccountDao.findByExternalId(gatewayAccountExternalId)
                .map(account -> partnerAppTokenDao.disableToken(token, account.getId()));
    }

    public Optional<PartnerAppTokenEntity> findByTokenAndGatewayAccountId(String token, String gatewatAccountId) {
        return gatewayAccountDao.findByExternalId(gatewatAccountId)
                .flatMap(account -> partnerAppTokenDao.findByTokenAndGatewayAccountId(token, account.getId()));
    }

    private PartnerAppTokenEntity insertToken(GatewayAccount account) {
        PartnerAppTokenEntity newToken = new PartnerAppTokenEntity();
        newToken.setGatewayAccountId(account.getId());
        newToken.setToken(RandomIdGenerator.newId());
        Long newId = partnerAppTokenDao.insert(newToken);
        newToken.setId(newId);
        return newToken;
    }
}
