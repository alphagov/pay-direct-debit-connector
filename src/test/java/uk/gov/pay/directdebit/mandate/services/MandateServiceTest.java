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
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.ConfirmMandateRequest;
import uk.gov.pay.directdebit.mandate.api.CreateMandateRequest;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.PaymentProviderFactory;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.Mandate.MandateBuilder.aMandate;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private MandateDao mandateDao;
    @Mock
    private GatewayAccountDao gatewayAccountDao;
    @Mock
    private TokenService tokenService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private MandateStateUpdateService mandateStateUpdateService;
    @Mock
    private DirectDebitConfig directDebitConfig;
    @Mock
    private LinksConfig linksConfig;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private UriBuilder uriBuilder;
    @Mock 
    private PaymentProviderFactory paymentProviderFactory;
    @Mock
    private SandboxService sandboxService;

    private MandateService service;

    @Before
    public void setUp() throws URISyntaxException {
        when(directDebitConfig.getLinks()).thenReturn(linksConfig);
        service = new MandateService(directDebitConfig, mandateDao, gatewayAccountDao, tokenService,
                transactionService,
                mandateStateUpdateService, paymentProviderFactory);
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build(any())).thenReturn(new URI("aaa"));
        when(linksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
    }

    @Test
    public void findMandateForToken_shouldUpdateTransactionStateAndRegisterEventWhenExchangingTokens() {
        String token = "token";
        Mandate mandate = MandateFixture.aMandateFixture().withState(CREATED).toEntity();
        when(mandateDao.findByTokenId(token))
                .thenReturn(Optional.of(mandate));
        when(mandateStateUpdateService.tokenExchangedFor(mandate)).thenReturn(mandate);
        TokenExchangeDetails tokenExchangeDetails = service.getMandateFor(token);
        assertThat(tokenExchangeDetails.getMandate(), is(mandate));
    }

    @Test
    public void shouldPopulateGetMandateResponseForFrontend() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        when(tokenService.generateNewTokenFor(mandate)).thenReturn(new Token("token", mandate.getId()));
        GetMandateResponse getMandateResponse = service.populateGetMandateResponse(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId(), uriInfo);
        assertThat(getMandateResponse.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(getMandateResponse.getMandateId(), is(mandate.getExternalId()));
        assertThat(getMandateResponse.getState(), is(mandate.getState().toExternal()));
    }

    @Test
    public void shouldPopulateGetMandateResponse() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
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
        when(transactionService.findTransactionForExternalId(mandateFixture.getExternalId().toString())).thenReturn(transactionFixture.toEntity());
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateWithTransactionResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId().toString());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
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

        assertThat(mandate.getMandateReference(), is(not(MandateBankStatementReference.valueOf("gocardless-default"))));
    }

    @Test
    public void shouldCreateAMandateForGoCardless_withCustomGeneratedReference() {
        Mandate mandate = getMandateForProvider(PaymentProvider.GOCARDLESS);

        assertThat(mandate.getMandateReference(), is(MandateBankStatementReference.valueOf("gocardless-default")));
    }

    @Test
    public void confirm_shouldConfirmOnDemandMandate() {
        GatewayAccount gatewayAccount = aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX).toEntity();
        Mandate mandate = getMandateForProvider(gatewayAccount);
        Map<String, String> confirmMandateRequest = Map.of("sort_code", "123456", "account_number", "12345678");
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);
        BankAccountDetails bankAccountDetails = BankAccountDetails.of(confirmMandateRequest);

        when(mandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(true);
        when(paymentProviderFactory.getCommandServiceFor(PaymentProvider.SANDBOX)).thenReturn(sandboxService);
        when(sandboxService.confirmOnDemandMandate(mandate, bankAccountDetails)).thenReturn(mandate);
        
        service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);

        verify(mandateStateUpdateService).confirmedOnDemandDirectDebitDetailsFor(mandate);
    }

    @Test
    public void confirm_shouldNotConfirmOnDemandMandateForInvalidState() {
        GatewayAccount gatewayAccount = aGatewayAccountFixture().withPaymentProvider(PaymentProvider.SANDBOX).toEntity();
        Mandate mandate = aMandate()
                .withGatewayAccount(gatewayAccount)
                .withExternalId(MandateExternalId.valueOf(RandomIdGenerator.newId()))
                .withMandateReference(MandateBankStatementReference.valueOf("mandateReference"))
                .withServiceReference("reference")
                .withState(MandateState.CANCELLED)
                .withReturnUrl("http://returnUrl")
                .withCreatedDate(ZonedDateTime.now(ZoneOffset.UTC))
                .build();

        Map<String, String> confirmMandateRequest = Map.of("sort_code", "123456", "account_number", "12345678");
        ConfirmMandateRequest mandateConfirmationRequest = ConfirmMandateRequest.of(confirmMandateRequest);

        when(mandateStateUpdateService.canUpdateStateFor(mandate, DirectDebitEvent.SupportedEvent.DIRECT_DEBIT_DETAILS_CONFIRMED)).thenReturn(false);
        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition DIRECT_DEBIT_DETAILS_CONFIRMED from state CANCELLED is not valid");

        service.confirm(gatewayAccount, mandate, mandateConfirmationRequest);
    }

    @Test
    public void shouldThrowUnlinkedGCAccountException_onMandateCreationWithUnlinkedAccount() {
        thrown.expect(UnlinkedGCMerchantAccountException.class);
        final String EXTERNAL_ID = "external1d";
        final String DESCRIPTION = "is awesome";
        GatewayAccount gatewayAccount = aGatewayAccountFixture()
                .withExternalId(EXTERNAL_ID)
                .withDescription(DESCRIPTION)
                .withPaymentProvider(PaymentProvider.GOCARDLESS)
                .withType(GatewayAccount.Type.TEST)
                .withAccessToken(null)
                .toEntity();
        when(gatewayAccountDao.findByExternalId(gatewayAccount.getExternalId())).thenReturn(Optional.of(gatewayAccount));
        service.createMandate(null, gatewayAccount.getExternalId());
    }

    @Test
    public void shouldThrowGatewayAccountNotFoundException_onMandateCreationWithInvalidAccount() {
        thrown.expect(GatewayAccountNotFoundException.class);
        service.createMandate(null, "test");
    }


    private Map<String, String> getMandateRequestPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("agreement_type", "ON_DEMAND");
        payload.put("return_url", "https://example.com/return");
        payload.put("service_reference", "some-service-reference");
        return payload;
    }

    private Mandate getMandateForProvider(GatewayAccount gatewayAccount) {
        when(gatewayAccountDao.findByExternalId(anyString())).thenReturn(Optional.of(gatewayAccount));
        when(mandateDao.insert(any(Mandate.class))).thenReturn(1L);

        CreateMandateRequest createMandateRequest = CreateMandateRequest.of(getMandateRequestPayload());
        Mandate mandate = service.createMandate(createMandateRequest, gatewayAccount.getExternalId());

        verify(mandateStateUpdateService).mandateCreatedFor(mandate);
        return mandate;
    }

    private Mandate getMandateForProvider(PaymentProvider paymentProvider) {
        GatewayAccount gatewayAccount = aGatewayAccountFixture().withPaymentProvider(paymentProvider).toEntity();
        return getMandateForProvider(gatewayAccount);
    }
}
