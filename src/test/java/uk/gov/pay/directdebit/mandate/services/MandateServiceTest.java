package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.events.model.GoCardlessEvent;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private MandateDao mockedMandateDao;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private TokenService mockedTokenService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private MandateStateUpdateService mockedMandateStateUpdateService;
    @Mock
    private DirectDebitConfig mockedDirectDebitConfig;
    @Mock
    private LinksConfig mockedLinksConfig;
    @Mock
    private UriInfo mockedUriInfo;
    @Mock
    private UriBuilder mockedUriBuilder;
    @Mock
    private GoCardlessEventDao mockedGoCardlessEventDao;

    private MandateService service;

    @Before
    public void setUp() throws URISyntaxException {
        when(mockedDirectDebitConfig.getLinks()).thenReturn(mockedLinksConfig);
        service = new MandateService(mockedDirectDebitConfig, mockedMandateDao, mockedGatewayAccountDao, mockedTokenService,
                mockedTransactionService,
                mockedMandateStateUpdateService,
                mockedGoCardlessEventDao);
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.path(anyString())).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.build(any())).thenReturn(new URI("aaa"));
        when(mockedLinksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
    }

    @Test
    public void findMandateForToken_shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() {
        String token = "token";
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withMandateType(MandateType.ON_DEMAND)
                .withState(CREATED)
                .toEntity();
        when(mockedMandateDao.findByTokenId(token))
                .thenReturn(Optional.of(mandate));
        when(mockedMandateStateUpdateService.tokenExchangedFor(mandate)).thenReturn(mandate);
        TokenExchangeDetails tokenExchangeDetails = service.getMandateFor(token);
        assertThat(tokenExchangeDetails.getMandate(), is(mandate));
        assertThat(tokenExchangeDetails.getTransactionExternalId(), is(nullValue()));
    }

    @Test
    public void shouldPopulateGetMandateResponseForFrontend() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        when(mockedTokenService.generateNewTokenFor(mandate)).thenReturn(new Token("token", mandate.getId()));
        GetMandateResponse getMandateResponse = service.populateGetMandateResponse(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId(), mockedUriInfo);
        assertThat(getMandateResponse.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(getMandateResponse.getMandateId(), is(mandate.getExternalId()));
        assertThat(getMandateResponse.getMandateType(), is(mandate.getType()));
        assertThat(getMandateResponse.getState(), is(mandate.getState().toExternal()));
    }

    @Test
    public void shouldPopulateGetMandateResponse() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(mandateResponseForFrontend.getMandateType(), is(mandate.getType().toString()));
        assertThat(mandateResponseForFrontend.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(mandateResponseForFrontend.getTransaction(), is(nullValue()));
    }

    @Test
    public void shouldPopulateGetMandateResponseForFrontend_whenThereIsATransaction() {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = MandateFixture
                .aMandateFixture()
                .withPayerFixture(payerFixture)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS);
        TransactionFixture transactionFixture = TransactionFixture
                .aTransactionFixture()
                .withMandateFixture(mandateFixture);
        Mandate mandate = mandateFixture.toEntity();
        when(mockedTransactionService.findTransactionForExternalId(mandateFixture.getExternalId().toString())).thenReturn(transactionFixture.toEntity());
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateWithTransactionResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId().toString());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(mandateResponseForFrontend.getMandateType(), is(mandate.getType().toString()));
        assertThat(mandateResponseForFrontend.getGatewayAccountExternalId(), is(mandate.getGatewayAccount().getExternalId()));
        assertThat(mandateResponseForFrontend.getGatewayAccountId(), is(mandate.getGatewayAccount().getId()));
        assertThat(mandateResponseForFrontend.getCreatedDate(), is(mandate.getCreatedDate()));
        assertThat(mandateResponseForFrontend.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(mandateResponseForFrontend.getPayer().getExternalId(), is(payerFixture.getExternalId()));
        assertThat(mandateResponseForFrontend.getPayer().getName(), is(payerFixture.getName()));
        assertThat(mandateResponseForFrontend.getPayer().getEmail(), is(payerFixture.getEmail()));
        assertThat(mandateResponseForFrontend.getPayer().getAccountRequiresAuthorisation(), is(payerFixture.getAccountRequiresAuthorisation()));
        assertThat(mandateResponseForFrontend.getTransaction().getAmount(), is(transactionFixture.getAmount()));
        assertThat(mandateResponseForFrontend.getTransaction().getDescription(), is(transactionFixture.getDescription()));
        assertThat(mandateResponseForFrontend.getTransaction().getExternalId(), is(transactionFixture.getExternalId()));
        assertThat(mandateResponseForFrontend.getTransaction().getState(), is(transactionFixture.getState().toExternal()));
        assertThat(mandateResponseForFrontend.getTransaction().getReference(), is(transactionFixture.getReference()));
    }

    @Test
    public void shouldCreateAMandateForSandbox_withCustomGeneratedReference() {
        Mandate mandate = getMandateForProvider(PaymentProvider.SANDBOX);

        assertThat(mandate.getMandateReference(), is(not("gocardless-default")));
    }

    @Test
    public void shouldCreateAMandateForGoCardless_withCustomGeneratedReference() {
        Mandate mandate = getMandateForProvider(PaymentProvider.GOCARDLESS);

        assertThat(mandate.getMandateReference(), is("gocardless-default"));
    }
    
    @Test
    public void shouldUpdateMandateStatusTo_CREATED_ForEventAction_CREATED() {
        Mandate mandate = getMandateForProvider(PaymentProvider.GOCARDLESS);
        List<GoCardlessEvent> goCardlessEvents = List.of(GoCardlessEvent.GoCardlessEventBuilder.aGoCardlessEvent()
                .withAction("CREATED")
                .build());
        
        when(mockedGoCardlessEventDao.findEventsForMandate(mandate.getExternalId().toString()))
                .thenReturn(goCardlessEvents);
        when(mockedMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        
        service.updateMandateStatus(mandate.getExternalId());
        
        verify(mockedMandateDao, times(1)).updateState(mandate.getId(), CREATED);
    }

    private Map<String, String> getMandateRequestPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("agreement_type", "ON_DEMAND");
        payload.put("return_url", "https://example.com/return");
        payload.put("service_reference", "some-service-reference");
        return payload;
    }

    private Mandate getMandateForProvider(PaymentProvider paymentProvider) {
        GatewayAccount gatewayAccount =
                GatewayAccountFixture.aGatewayAccountFixture().withPaymentProvider(paymentProvider).toEntity();
        when(mockedGatewayAccountDao.findByExternalId(anyString())).thenReturn(Optional.of(gatewayAccount));
        when(mockedMandateDao.insert(any(Mandate.class))).thenReturn(1L);

        CreateMandateRequest createMandateRequest = CreateMandateRequest.of(getMandateRequestPayload());

        Mandate mandate = service.createMandate(createMandateRequest, gatewayAccount.getExternalId());

        verify(mockedMandateStateUpdateService).mandateCreatedFor(mandate);
        return mandate;
    }
}
