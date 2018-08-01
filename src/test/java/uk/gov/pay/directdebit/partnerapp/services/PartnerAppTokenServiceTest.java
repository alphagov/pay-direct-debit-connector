package uk.gov.pay.directdebit.partnerapp.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.dao.PartnerAppTokenDao;
import uk.gov.pay.directdebit.partnerapp.fixtures.PartnerAppTokenEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PartnerAppTokenServiceTest {

    @Mock
    private PartnerAppTokenDao mockedTokenDao;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;

    private PartnerAppTokenService tokenService;
    private final GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Before
    public void setUp() {
        tokenService = new PartnerAppTokenService(mockedTokenDao, mockedGatewayAccountDao);
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
    }

    @Test
    public void shouldCreateAToken_whenNoTokenExists() {
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.empty());
        Optional<PartnerAppTokenEntity> thisToken = tokenService.createToken(gatewayAccount.getExternalId());
        verify(mockedTokenDao).insert(thisToken.get());
    }

    @Test
    public void shouldCreateAToken_whenTokenExists_andDisableExistingOne() {
        PartnerAppTokenEntity tokenEntity = PartnerAppTokenEntityFixture.aPartnerAppTokenFixture()
                .withGatewayAccountId(gatewayAccount.getId()).toEntity();
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.of(tokenEntity));
        Optional<PartnerAppTokenEntity> thisToken = tokenService.createToken(gatewayAccount.getExternalId());
        verify(mockedTokenDao).disableToken(tokenEntity.getToken(), gatewayAccount.getId());
        verify(mockedTokenDao).insert(thisToken.get());
    }

    @Test
    public void shouldFindATokenByTokenAndGatewayAccountId() {
        PartnerAppTokenEntity tokenEntity = PartnerAppTokenEntityFixture.aPartnerAppTokenFixture()
                .withGatewayAccountId(gatewayAccount.getId()).toEntity();
        when(mockedTokenDao.findByTokenAndGatewayAccountId(tokenEntity.getToken(), gatewayAccount.getId())).thenReturn(Optional.of(tokenEntity));
        PartnerAppTokenEntity thisToken = tokenService.findByTokenAndGatewayAccountId(tokenEntity.getToken(), gatewayAccount.getExternalId()).get();
        assertThat(thisToken.getToken(), is(tokenEntity.getToken()));
    }
}
