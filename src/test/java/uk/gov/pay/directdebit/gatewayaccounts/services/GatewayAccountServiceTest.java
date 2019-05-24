package uk.gov.pay.directdebit.gatewayaccounts.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.api.CreateGatewayAccountRequest;
import uk.gov.pay.directdebit.gatewayaccounts.api.GatewayAccountResponse;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class GatewayAccountServiceTest {
    private static final PaymentProvider PAYMENT_PROVIDER = PaymentProvider.SANDBOX;
    private static final String SERVICE_NAME = "alex";
    private static final String DESCRIPTION = "is awesome";
    private static final String ANALYTICS_ID = "DD_234098_BBBLA";
    private static final GatewayAccount.Type TYPE = GatewayAccount.Type.TEST;

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture()
            .withPaymentProvider(PAYMENT_PROVIDER)
            .withServiceName(SERVICE_NAME)
            .withDescription(DESCRIPTION)
            .withType(TYPE)
            .withAnalyticsId(ANALYTICS_ID);

    private MandateFixture mandateFixture = MandateFixture.aMandateFixture()
            .withGatewayAccountFixture(gatewayAccountFixture);
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;

    @Mock
    private UriInfo mockedUriInfo;

    @Mock
    private UriBuilder mockedUriBuilder;

    private GatewayAccountService service;

    private Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
        service = new GatewayAccountService(mockedGatewayAccountDao);
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.path(anyString())).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.build(any())).thenReturn(new URI("http://www.example.com/"));
    }

    @Test
    public void shouldReturnGatewayAccountIfItExists() {
        String accountId = "10sadsadsadL";
        when(mockedGatewayAccountDao.findByExternalId(accountId)).thenReturn(Optional.of(gatewayAccountFixture.toEntity()));
        GatewayAccount gatewayAccount = service.getGatewayAccountForId(accountId);
        assertThat(gatewayAccount.getId(), is(gatewayAccountFixture.getId()));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }

    @Test
    public void shouldThrowGatewayAccountNotFoundExceptionIfGatewayAccountDoesNotExist() {
        String accountId = "10sadsadsadL";
        when(mockedGatewayAccountDao.findByExternalId(accountId)).thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: 10");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.getGatewayAccountForId(accountId);
    }

    @Test
    public void shouldReturnGatewayAccountForTransactionIfItExists() {
        when(mockedGatewayAccountDao.findById(gatewayAccountFixture.getId()))
                .thenReturn(Optional.of(gatewayAccountFixture.toEntity()));
        GatewayAccount gatewayAccount = service.getGatewayAccountFor(transaction);
        assertThat(gatewayAccount.getId(), is(gatewayAccountFixture.getId()));
        assertThat(gatewayAccount.getPaymentProvider(), is(PAYMENT_PROVIDER));
        assertThat(gatewayAccount.getAnalyticsId(), is(ANALYTICS_ID));
        assertThat(gatewayAccount.getDescription(), is(DESCRIPTION));
        assertThat(gatewayAccount.getType(), is(TYPE));
    }

    @Test
    public void shouldThrowIfGatewayAccountForTransactionDoesNotExist() {
        when(mockedGatewayAccountDao.findById(gatewayAccountFixture.getId()))
                .thenReturn(Optional.empty());
        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: " + gatewayAccountFixture.getId());
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.getGatewayAccountFor(transaction);
    }

    @Test
    public void shouldReturnAListOfGatewayAccounts() {
        when(mockedGatewayAccountDao.findAll())
                .thenReturn(Arrays.asList(gatewayAccountFixture.toEntity(),
                        gatewayAccountFixture.toEntity(),
                        gatewayAccountFixture.toEntity()));

        List<GatewayAccountResponse> gatewayAccounts = service.getAllGatewayAccounts("", mockedUriInfo);

        assertThat(gatewayAccounts.size(), is(3));
        assertThat(gatewayAccounts.get(0).getLinks().size(), is(1));
        assertThat(gatewayAccounts.get(0).getSelfLink(), is(notNullValue()));
        verify(mockedGatewayAccountDao).findAll();
    }

    @Test
    public void shouldReturnAListOfGatewayAccountsSingle() {
        GatewayAccount fixture = gatewayAccountFixture.toEntity();
        when(mockedGatewayAccountDao.find(any(List.class)))
                .thenReturn(Collections.singletonList(fixture));

        List<GatewayAccountResponse> gatewayAccounts = service.getAllGatewayAccounts(fixture.getExternalId(), mockedUriInfo);

        assertThat(gatewayAccounts.size(), is(1));
        assertThat(gatewayAccounts.get(0).getLinks().size(), is(1));
        assertThat(gatewayAccounts.get(0).getSelfLink(), is(notNullValue()));
        verify(mockedGatewayAccountDao).find(any(List.class));
    }

    @Test
    public void shouldReturnAListOfGatewayAccountsMultiple() {
        GatewayAccount fixture1 = gatewayAccountFixture.toEntity();
        GatewayAccount fixture2 = gatewayAccountFixture.toEntity();
        List<String> ids = Arrays.asList(fixture1.getExternalId(), fixture2.getExternalId());

        when(mockedGatewayAccountDao.find(any(List.class)))
                .thenReturn(Arrays.asList(fixture1, fixture2));

        List<GatewayAccountResponse> gatewayAccounts = service.getAllGatewayAccounts(String.join(",", ids), mockedUriInfo);

        assertThat(gatewayAccounts.size(), is(2));
        assertThat(gatewayAccounts.get(0).getLinks().size(), is(1));
        assertThat(gatewayAccounts.get(0).getSelfLink(), is(notNullValue()));
        verify(mockedGatewayAccountDao).find(any(List.class));
    }

    @Test
    public void shouldStoreAGatewayAccount() {
        CreateGatewayAccountRequest request = new CreateGatewayAccountRequest(PaymentProvider.SANDBOX, GatewayAccount.Type.TEST, 
                        "aDescription", "123", PaymentProviderAccessToken.of("token"), GoCardlessOrganisationId.valueOf("provider"));
        ArgumentCaptor<GatewayAccount> capturedGatewayAccount = ArgumentCaptor.forClass(GatewayAccount.class);
        service.create(request);
        verify(mockedGatewayAccountDao).insert(capturedGatewayAccount.capture());
        assertThat(capturedGatewayAccount.getValue().getPaymentProvider(), is(PaymentProvider.SANDBOX));
    }
    
    @Test
    public void shouldUpdateAGatewayAccount() {
        String externalAccountId = "an-external-id";
        GatewayAccount gatewayAccount = GatewayAccountFixture
                .aGatewayAccountFixture()
                .withExternalId(externalAccountId)
                .toEntity();
        when(mockedGatewayAccountDao.findByExternalId(externalAccountId)).thenReturn(Optional.of(gatewayAccount));

        ImmutableMap<String, String> accessTokenPayload = ImmutableMap.<String, String>builder()
                .put("op", "replace")
                .put("path", "access_token")
                .put("value", "abcde1234")
                .build();
        ImmutableMap<String, String> organisationPayload =
                ImmutableMap.<String, String>builder()
                        .put("op", "replace")
                        .put("path", "organisation")
                        .put("value", "1234abcde")
                        .build();

        service.patch(externalAccountId, Arrays.asList(accessTokenPayload, organisationPayload));
        verify(mockedGatewayAccountDao).updateAccessTokenAndOrganisation(externalAccountId,
                PaymentProviderAccessToken.of("abcde1234"), GoCardlessOrganisationId.valueOf("1234abcde"));
    }
}
