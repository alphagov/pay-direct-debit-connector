package uk.gov.pay.directdebit.partnerapp.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.client.GoCardlessConnectClient;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessConnectClientResponse;
import uk.gov.pay.directdebit.partnerapp.fixtures.GoCardlessConnectClientResponseBuilder;
import uk.gov.pay.directdebit.partnerapp.dao.PartnerAppTokenDao;
import uk.gov.pay.directdebit.partnerapp.fixtures.PartnerAppTokenEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PartnerAppTokenServiceTest {
    
    private static final String REDIRECT_URI = "https://example.com/oauth/complete";

    @Mock
    private PartnerAppTokenDao mockedTokenDao;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private GoCardlessConnectClient mockedConnectClient;

    private PartnerAppTokenService tokenService;
    private final GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Before
    public void setUp() {
        tokenService = new PartnerAppTokenService(mockedTokenDao, mockedGatewayAccountDao, mockedConnectClient);
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
    }

    @Test
    public void shouldCreateAToken_whenNoTokenExists() {
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.empty());
        PartnerAppTokenEntity thisToken = tokenService.createToken(gatewayAccount.getExternalId(), REDIRECT_URI).get();
        verify(mockedTokenDao).insert(thisToken);
        verifyEntity(thisToken, gatewayAccount.getId());
    }

    @Test
    public void shouldCreateAToken_whenTokenExists_andDisableExistingOne() {
        PartnerAppTokenEntity tokenEntity = PartnerAppTokenEntityFixture.aPartnerAppTokenFixture()
                .withGatewayAccountId(gatewayAccount.getId()).toEntity();
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.of(tokenEntity));
        PartnerAppTokenEntity thisToken = tokenService.createToken(gatewayAccount.getExternalId(), REDIRECT_URI).get();
        verify(mockedTokenDao).disableToken(tokenEntity.getToken(), gatewayAccount.getId());
        verify(mockedTokenDao).insert(thisToken);
        verifyEntity(thisToken, gatewayAccount.getId());
    }

    @Test
    public void shouldUpdatedGatewayAccount_afterSuccessfulResponse_fromGoCardlessConnect() {
        String accessCode = "some-test-access-code";
        String partnerToken = "some-test-partner-token";
        PartnerAppTokenEntity tokenEntity = PartnerAppTokenEntityFixture.aPartnerAppTokenFixture()
                .withGatewayAccountId(gatewayAccount.getId())
                .withToken(partnerToken)
                .toEntity();
        when(mockedTokenDao.findActiveTokenByToken(partnerToken)).thenReturn(Optional.of(tokenEntity));
        when(mockedGatewayAccountDao.findById(gatewayAccount.getId())).thenReturn(Optional.of(gatewayAccount));
        GoCardlessConnectClientResponse response = GoCardlessConnectClientResponseBuilder
                .aGoCardlessConnectClientResponse()
                .build();
        when(mockedConnectClient.postAccessCode(accessCode, gatewayAccount, REDIRECT_URI)).thenReturn(Optional.of(response));
        tokenService.exchangeCodeForToken(accessCode, partnerToken);
        verify(mockedGatewayAccountDao).updateAccessTokenAndOrganisation(
                gatewayAccount.getExternalId(),
                response.getAccessToken(),
                response.getOrganisationId()
        );
    }

    private void verifyEntity(PartnerAppTokenEntity thisToken, Long gatewayAccountId) {
        assertThat(thisToken.getGatewayAccountId(), is(gatewayAccountId));
        assertThat(thisToken.getToken(), is(notNullValue()));
        assertThat(thisToken.isActive(), is(Boolean.TRUE));
    }
}
