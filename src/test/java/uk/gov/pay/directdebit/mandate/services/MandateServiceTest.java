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
import uk.gov.pay.directdebit.common.exception.UnlinkedGCMerchantAccountException;
import uk.gov.pay.directdebit.events.services.GovUkPayEventService;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.MandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.services.PaymentQueryService;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_CREATED;
import static uk.gov.pay.directdebit.events.model.GovUkPayEventType.MANDATE_TOKEN_EXCHANGED;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.fromMandate;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private MandateDao mockMandateDao;

    @Mock
    private GatewayAccountDao mockGatewayAccountDao;

    @Mock
    private TokenService mockTokenService;

    @Mock
    private PaymentQueryService mockPaymentQueryService;

    @Mock
    private GovUkPayEventService mockGovUkPayEventService;

    @Mock
    private UserNotificationService mockUserNotificationService;

    @Mock
    private DirectDebitConfig mockDirectDebitConfig;

    @Mock
    private LinksConfig mockLinksConfig;

    @Mock
    private UriInfo mockUriInfo;

    @Mock
    private UriBuilder mockUriBuilder;

    @Mock
    private PaymentProviderFactory mockPaymentProviderFactory;

    private MandateService service;

    @Before
    public void setUp() throws URISyntaxException {
        when(mockDirectDebitConfig.getLinks()).thenReturn(mockLinksConfig);

        service = new MandateService(
                mockDirectDebitConfig,
                mockMandateDao,
                mockGatewayAccountDao,
                mockTokenService,
                mockPaymentProviderFactory,
                mockUserNotificationService,
                mockGovUkPayEventService,
                mockPaymentQueryService);

        when(mockUriInfo.getBaseUriBuilder()).thenReturn(mockUriBuilder);
        when(mockUriBuilder.path(anyString())).thenReturn(mockUriBuilder);
        when(mockUriBuilder.build(any())).thenReturn(new URI("aaa"));
        when(mockLinksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
    }

    @Test
    public void nextUrlAndNextUrlPostShouldOnlyBePresentWhenMandateStateIsCreated() {
        Arrays.stream(MandateState.values()).forEach(state -> {
            Mandate mandate = aMandateFixture().withState(state).toEntity();
            when(mockMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
            when(mockTokenService.generateNewTokenFor(mandate)).thenReturn(new Token("token", mandate.getId()));
            MandateResponse mandateResponse = service.populateGetMandateResponse(
                    mandate.getGatewayAccount().getExternalId(), mandate.getExternalId(), mockUriInfo);

            if (state == CREATED) {
                assertThat(mandateResponse.getLink("next_url").isPresent(), is(true));
                assertThat(mandateResponse.getLink("next_url_post").isPresent(), is(true));
            } else {
                assertThat(mandateResponse.getLink("next_url").isEmpty(), is(true));
                assertThat(mandateResponse.getLink("next_url_post").isEmpty(), is(true));
            }
        });

    }

    @Test
    public void findMandateForToken_shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() {
        String token = "token";
        Mandate mandate = aMandateFixture().withState(CREATED).toEntity();
        when(mockMandateDao.findByTokenId(token))
                .thenReturn(Optional.of(mandate));

        Mandate tokenExchangedMandate = aMandateFixture().withState(AWAITING_DIRECT_DEBIT_DETAILS).toEntity();
        when(mockGovUkPayEventService.storeEventAndUpdateStateForMandate(mandate, MANDATE_TOKEN_EXCHANGED))
                .thenReturn(tokenExchangedMandate);

        TokenExchangeDetails tokenExchangeDetails = service.getMandateFor(token);
        assertThat(tokenExchangeDetails.getMandate(), is(tokenExchangedMandate));
    }

    @Test
    public void shouldPopulateGetMandateResponseForFrontend() {
        Mandate mandate = aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        MandateResponse getMandateResponse = service.populateGetMandateResponse(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId(), mockUriInfo);
        assertThat(getMandateResponse.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(getMandateResponse.getMandateId(), is(mandate.getExternalId()));
        assertThat(getMandateResponse.getState().getMandateState(), is(mandate.getState().toExternal()));
    }

    @Test
    public void shouldPopulateGetMandateResponse() {
        Mandate mandate = aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateBankStatementReference().get()));
        assertThat(mandateResponseForFrontend.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(mandateResponseForFrontend.getPayment(), is(nullValue()));
    }

    @Test
    public void shouldPopulateGetMandateResponseForFrontend_whenThereIsATransaction() {
        PayerFixture payerFixture = PayerFixture.aPayerFixture();
        MandateFixture mandateFixture = aMandateFixture()
                .withPayerFixture(payerFixture)
                .withState(AWAITING_DIRECT_DEBIT_DETAILS);
        PaymentFixture paymentFixture = PaymentFixture
                .aPaymentFixture()
                .withMandateFixture(mandateFixture)
                .withStateDetails("state details");
        Mandate mandate = mandateFixture.toEntity();
        when(mockPaymentQueryService.findPaymentForExternalId(mandateFixture.getExternalId().toString())).thenReturn(paymentFixture.toEntity());
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateWithPaymentResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId().toString());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateBankStatementReference().get()));
        assertThat(mandateResponseForFrontend.getGatewayAccountExternalId(), is(mandate.getGatewayAccount().getExternalId()));
        assertThat(mandateResponseForFrontend.getGatewayAccountId(), is(mandate.getGatewayAccount().getId()));
        assertThat(mandateResponseForFrontend.getCreatedDate(), is(mandate.getCreatedDate()));
        assertThat(mandateResponseForFrontend.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(mandateResponseForFrontend.getPayer().getExternalId(), is(payerFixture.getExternalId()));
        assertThat(mandateResponseForFrontend.getPayer().getName(), is(payerFixture.getName()));
        assertThat(mandateResponseForFrontend.getPayer().getEmail(), is(payerFixture.getEmail()));
        assertThat(mandateResponseForFrontend.getPayer().getAccountRequiresAuthorisation(), is(payerFixture.getAccountRequiresAuthorisation()));
        assertThat(mandateResponseForFrontend.getPayment().getAmount(), is(paymentFixture.getAmount()));
        assertThat(mandateResponseForFrontend.getPayment().getDescription(), is(paymentFixture.getDescription()));
        assertThat(mandateResponseForFrontend.getPayment().getExternalId(), is(paymentFixture.getExternalId()));
        assertThat(mandateResponseForFrontend.getPayment().getState().getPaymentState(), is(paymentFixture.getState().toExternal()));
        assertThat(mandateResponseForFrontend.getPayment().getReference(), is(paymentFixture.getReference()));
    }

    @Test
    public void shouldCreateAMandateForSandbox_withCustomGeneratedReference() {
        Mandate mandate = getMandateForProvider(SANDBOX);

        assertThat(mandate.getMandateBankStatementReference(), is(not(MandateBankStatementReference.valueOf("gocardless-default"))));
    }

    @Test
    public void assertNullMandateBankStatementReferenceWhenCreatingMandate() {
        List.of(GOCARDLESS, SANDBOX).forEach(p -> {
            Mandate mandate = getMandateForProvider(p);
            assertThat(mandate.getMandateBankStatementReference().isEmpty(), is(true));
        });
    }

    @Test
    public void shouldThrowUnlinkedGCAccountException_onMandateCreationWithUnlinkedAccount() {
        thrown.expect(UnlinkedGCMerchantAccountException.class);
        final String EXTERNAL_ID = "external1d";
        final String DESCRIPTION = "is awesome";
        GatewayAccount gatewayAccount = aGatewayAccountFixture()
                .withExternalId(EXTERNAL_ID)
                .withDescription(DESCRIPTION)
                .withPaymentProvider(GOCARDLESS)
                .withType(GatewayAccount.Type.TEST)
                .withAccessToken(null)
                .toEntity();
        when(mockGatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        service.createMandate(null, gatewayAccount.getExternalId());
    }

    @Test
    public void shouldThrowGatewayAccountNotFoundException_onMandateCreationWithInvalidAccount() {
        thrown.expect(GatewayAccountNotFoundException.class);
        service.createMandate(null, "test");
    }

    private Mandate getMandateForProvider(GatewayAccount gatewayAccount) {
        when(mockGatewayAccountDao.findByExternalId(anyString())).thenReturn(Optional.of(gatewayAccount));
        when(mockMandateDao.insert(any(Mandate.class))).thenReturn(1L);

        when(mockGovUkPayEventService.storeEventAndUpdateStateForMandate(any(Mandate.class), eq(MANDATE_CREATED)))
                .then(invocationOnMock -> {
                    Mandate insertedMandate = invocationOnMock.getArgument(0, Mandate.class);
                    return fromMandate(insertedMandate).withState(CREATED).build();
                });

        var createMandateRequest = new CreateMandateRequest("https://example.com/return", "some-service-reference");
        Mandate mandate = service.createMandate(createMandateRequest, gatewayAccount.getExternalId());

        assertThat(mandate.getState(), is(CREATED));

        return mandate;
    }

    private Mandate getMandateForProvider(PaymentProvider paymentProvider) {
        GatewayAccount gatewayAccount = aGatewayAccountFixture().withPaymentProvider(paymentProvider).toEntity();
        return getMandateForProvider(gatewayAccount);
    }
}
