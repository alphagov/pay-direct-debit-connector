package uk.gov.pay.directdebit.partnerapp.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.exception.BadRequestException;
import uk.gov.pay.directdebit.common.exception.GoCardlessAccountAlreadyConnectedException;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.partnerapp.api.GoCardlessAppConnectStateResponse;
import uk.gov.pay.directdebit.partnerapp.client.GoCardlessAppConnectClient;
import uk.gov.pay.directdebit.partnerapp.client.model.GoCardlessAppConnectAccessTokenResponse;
import uk.gov.pay.directdebit.partnerapp.dao.GoCardlessAppConnectAccountTokenDao;
import uk.gov.pay.directdebit.partnerapp.fixtures.GoCardlessAppConnectClientResponseBuilder;
import uk.gov.pay.directdebit.partnerapp.fixtures.GoCardlessAppConnectAccountEntityFixture;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GoCardlessAppConnectAccountServiceTest {

    private static final String REDIRECT_URI = "https://example.com/oauth/complete";

    @Mock
    private GoCardlessAppConnectAccountTokenDao mockedTokenDao;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private GoCardlessAppConnectClient mockedConnectClient;
    @Captor
    private ArgumentCaptor<GoCardlessAppConnectAccountEntity> captor;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GoCardlessAppConnectAccountService tokenService;
    private final GatewayAccount gatewayAccount = GatewayAccountFixture.aGatewayAccountFixture().toEntity();

    @Before
    public void setUp() {
        tokenService = new GoCardlessAppConnectAccountService(mockedTokenDao, mockedGatewayAccountDao, mockedConnectClient);
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
    }

    @Test
    public void shouldCreateAToken_whenNoTokenExists() {
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.empty());
        Response response = tokenService.createToken(gatewayAccount.getExternalId(), REDIRECT_URI);
        GoCardlessAppConnectStateResponse tokenResponse = (GoCardlessAppConnectStateResponse) response.getEntity();
        verify(mockedTokenDao, never()).disableToken(anyString(), anyLong());
        verify(mockedTokenDao).insert(captor.capture());
        GoCardlessAppConnectAccountEntity entity = captor.getValue();
        assertThat(tokenResponse.getState(), is(entity.getToken()));
        assertThat(tokenResponse.isActive(), is(Boolean.TRUE));
        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getLocation(), is(URI.create("/v1/api/gocardless/partnerapp/tokens/" + tokenResponse.getState())));

    }

    @Test
    public void shouldCreateAToken_whenTokenExists_andDisableExistingOne() {
        GoCardlessAppConnectAccountEntity accountEntity = GoCardlessAppConnectAccountEntityFixture.aPartnerAppAccountFixture()
                .withGatewayAccountId(gatewayAccount.getId()).toEntity();
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        when(mockedTokenDao.findByGatewayAccountId(gatewayAccount.getId())).thenReturn(Optional.of(accountEntity));
        GoCardlessAppConnectStateResponse response = (GoCardlessAppConnectStateResponse) tokenService.createToken(gatewayAccount.getExternalId(), REDIRECT_URI).getEntity();
        verify(mockedTokenDao).disableToken(accountEntity.getToken(), gatewayAccount.getId());
        verify(mockedTokenDao).insert(captor.capture());
        GoCardlessAppConnectAccountEntity entity = captor.getValue();
        assertThat(response.getState(), is(entity.getToken()));
        assertThat(response.isActive(), is(Boolean.TRUE));
    }

    @Test
    public void shouldThrowException_whenNoGatewayAccountIsFound() {
        when(mockedGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.empty());
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("There is no gateway account with external id");
        thrown.reportMissingExceptionWithMessage("BadRequestException expected");
        tokenService.createToken(gatewayAccount.getExternalId(), REDIRECT_URI).getEntity();
    }

    @Test
    public void shouldUpdatedGatewayAccount_afterSuccessfulResponse_fromGoCardlessConnect() {
        String accessCode = "some-test-access-code";
        String partnerToken = "some-test-partner-token";
        GoCardlessAppConnectAccountEntity accountEntity = GoCardlessAppConnectAccountEntityFixture.aPartnerAppAccountFixture()
                .withGatewayAccountId(gatewayAccount.getId())
                .withToken(partnerToken)
                .toEntity();
        when(mockedTokenDao.findActiveTokenByToken(partnerToken)).thenReturn(Optional.of(accountEntity));
        when(mockedGatewayAccountDao.findById(gatewayAccount.getId())).thenReturn(Optional.of(gatewayAccount));
        GoCardlessAppConnectAccessTokenResponse response = GoCardlessAppConnectClientResponseBuilder
                .aGoCardlessConnectClientResponse()
                .build();
        when(mockedGatewayAccountDao.existsWithOrganisation(response.getOrganisationId())).thenReturn(false);
        when(mockedConnectClient.postAccessCode(accessCode, gatewayAccount, REDIRECT_URI)).thenReturn(Optional.of(response));
        tokenService.exchangeCodeForToken(accessCode, partnerToken);
        verify(mockedGatewayAccountDao).updateAccessTokenAndOrganisation(
                gatewayAccount.getExternalId(),
                response.getAccessToken(),
                response.getOrganisationId()
        );
    }
    
    @Test
    public void shouldThrowException_whenGatewayAccountAlreadyExistsWithOrganisationId() {
        String accessCode = "some-test-access-code";
        String partnerToken = "some-test-partner-token";
        GoCardlessAppConnectAccountEntity accountEntity = GoCardlessAppConnectAccountEntityFixture.aPartnerAppAccountFixture()
                .withGatewayAccountId(gatewayAccount.getId())
                .withToken(partnerToken)
                .toEntity();
        when(mockedTokenDao.findActiveTokenByToken(partnerToken)).thenReturn(Optional.of(accountEntity));
        when(mockedGatewayAccountDao.findById(gatewayAccount.getId())).thenReturn(Optional.of(gatewayAccount));
        GoCardlessAppConnectAccessTokenResponse response = GoCardlessAppConnectClientResponseBuilder
                .aGoCardlessConnectClientResponse()
                .build();
        when(mockedGatewayAccountDao.existsWithOrganisation(response.getOrganisationId())).thenReturn(true);
        when(mockedConnectClient.postAccessCode(accessCode, gatewayAccount, REDIRECT_URI)).thenReturn(Optional.of(response));
        
        thrown.expect(GoCardlessAccountAlreadyConnectedException.class);
        
        tokenService.exchangeCodeForToken(accessCode, partnerToken);
    }
}
