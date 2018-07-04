package uk.gov.pay.directdebit.mandate.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.api.DirectDebitInfoFrontendResponse;
import uk.gov.pay.directdebit.mandate.api.GetMandateResponse;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.fixtures.DirectDebitEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.DirectDebitEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.model.TokenExchangeDetails;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.model.MandateState.ACTIVE;
import static uk.gov.pay.directdebit.mandate.model.MandateState.AWAITING_DIRECT_DEBIT_DETAILS;
import static uk.gov.pay.directdebit.mandate.model.MandateState.CREATED;
import static uk.gov.pay.directdebit.mandate.model.MandateState.PENDING;
import static uk.gov.pay.directdebit.mandate.model.MandateState.SUBMITTED;
import static uk.gov.pay.directdebit.payments.model.DirectDebitEvent.SupportedEvent.MANDATE_PENDING;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private DirectDebitEventService mockedDirectDebitEventService;
    @Mock
    private MandateDao mockedMandateDao;
    @Mock
    private GatewayAccountDao mockedGatewayAccountDao;
    @Mock
    private TokenService mockedTokenService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private DirectDebitConfig mockedDirectDebitConfig;
    @Mock
    private LinksConfig mockedLinksConfig;
    @Mock
    private UserNotificationService mockedUserNotificationService;
    @Mock
    private UriInfo mockedUriInfo;
    @Mock
    private UriBuilder mockedUriBuilder;

    private MandateService service;

    @Before
    public void setUp() throws URISyntaxException {
        when(mockedDirectDebitConfig.getLinks()).thenReturn(mockedLinksConfig);
        service = new MandateService(mockedDirectDebitConfig, mockedMandateDao, mockedGatewayAccountDao, mockedTokenService,
                mockedTransactionService, mockedDirectDebitEventService, mockedUserNotificationService);
        when(mockedUriInfo.getBaseUriBuilder()).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.path(anyString())).thenReturn(mockedUriBuilder);
        when(mockedUriBuilder.build(any())).thenReturn(new URI("aaa"));
        when(mockedLinksConfig.getFrontendUrl()).thenReturn("https://frontend.test");
    }

    @Test
    public void shouldUpdateMandateStateRegisterEventAndSendEmail_whenMandateFails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateFailedFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateFailedEventFor(mandate);
        verify(mockedUserNotificationService).sendMandateFailedEmailFor(mandate);
    }

    @Test
    public void shouldUpdateMandateStateRegisterEventAndSendEmail_whenMandateIsCancelled() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateCancelledFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateCancelledEventFor(mandate);
        verify(mockedUserNotificationService).sendMandateCancelledEmailFor(mandate);
    }

    @Test
    public void mandateActiveFor_shouldRegisterAMandateActiveEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        service.mandateActiveFor(mandate);

        verify(mockedDirectDebitEventService).registerMandateActiveEventFor(mandate);
        assertThat(mandate.getState(), is(ACTIVE));
    }


    @Test
    public void findMandatePendingEventFor_shouldFindEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(PENDING)
                .toEntity();
        DirectDebitEvent directDebitEvent = DirectDebitEventFixture.aDirectDebitEventFixture().toEntity();

        when(mockedDirectDebitEventService
                .findBy(mandate.getId(), DirectDebitEvent.Type.MANDATE, MANDATE_PENDING))
                .thenReturn(Optional.of(directDebitEvent));

        DirectDebitEvent foundDirectDebitEvent = service.findMandatePendingEventFor(mandate).get();

        assertThat(foundDirectDebitEvent, is(directDebitEvent));
    }

    @Test
    public void payerCreatedFor_shouldRegisterAPayerCreatedEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        service.payerCreatedFor(mandate);

        verify(mockedDirectDebitEventService).registerPayerCreatedEventFor(mandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void payerEditedFor_shouldRegisterAPayerEditedEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        service.payerEditedFor(mandate);

        verify(mockedDirectDebitEventService).registerPayerEditedEventFor(mandate);
        verifyZeroInteractions(mockedMandateDao);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldRegisterEventWhenReceivingDirectDebitDetails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedMandateDao.findByExternalId(mandate.getExternalId()))
                .thenReturn(Optional.of(mandate));
        service.receiveDirectDebitDetailsFor(mandate.getExternalId());
        verify(mockedDirectDebitEventService).registerDirectDebitReceivedEventFor(mandate);
        assertThat(mandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
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
        TokenExchangeDetails tokenExchangeDetails = service.getMandateFor(token);
        Mandate newMandate = tokenExchangeDetails.getMandate();
        assertThat(newMandate.getId(), is(notNullValue()));
        assertThat(newMandate.getExternalId(), is(mandate.getExternalId()));
        assertThat(newMandate.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(newMandate.getGatewayAccount(), is(mandate.getGatewayAccount()));
        assertThat(newMandate.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(newMandate.getServiceReference(), is(mandate.getServiceReference()));
        assertThat(newMandate.getPayer(), is(mandate.getPayer()));
        assertThat(newMandate.getType(), is(mandate.getType()));
        assertThat(newMandate.getState(), is(AWAITING_DIRECT_DEBIT_DETAILS));
        assertThat(newMandate.getCreatedDate(), is(mandate.getCreatedDate()));
        verify(mockedDirectDebitEventService).registerTokenExchangedEventFor(newMandate);
    }

    @Test
    public void shouldUpdateMandateStateAndRegisterEventWhenConfirmingDirectDebitDetails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();
        when(mockedMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        Mandate newMandate = service.confirmedDirectDebitDetailsFor(mandate.getExternalId());
        assertThat(newMandate.getId(), is(notNullValue()));
        assertThat(newMandate.getExternalId(), is(mandate.getExternalId()));
        assertThat(newMandate.getReturnUrl(), is(mandate.getReturnUrl()));
        assertThat(newMandate.getGatewayAccount(), is(mandate.getGatewayAccount()));
        assertThat(newMandate.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(newMandate.getServiceReference(), is(mandate.getServiceReference()));
        assertThat(newMandate.getPayer(), is(mandate.getPayer()));
        assertThat(newMandate.getType(), is(mandate.getType()));
        assertThat(newMandate.getState(), is(SUBMITTED));
        assertThat(newMandate.getCreatedDate(), is(mandate.getCreatedDate()));
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
        when(mockedTransactionService.findTransactionForExternalId(mandateFixture.getExternalId())).thenReturn(transactionFixture.toEntity());
        DirectDebitInfoFrontendResponse mandateResponseForFrontend = service.populateGetMandateWithTransactionResponseForFrontend(mandate.getGatewayAccount().getExternalId(), mandate.getExternalId());
        assertThat(mandateResponseForFrontend.getMandateReference(), is(mandate.getMandateReference()));
        assertThat(mandateResponseForFrontend.getMandateType(), is(mandate.getType().toString()));
        assertThat(mandateResponseForFrontend.getGatewayAccountExternalId(), is(mandate.getGatewayAccount().getExternalId()));
        assertThat(mandateResponseForFrontend.getGatewayAccountId(), is(mandate.getGatewayAccount().getId()));
        assertThat(mandateResponseForFrontend.getCreatedDate(), is(mandate.getCreatedDate().toString()));
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
    public void mandatePendingFor_shouldRegisterAMandatePendingEvent() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(SUBMITTED)
                .toEntity();

        service.mandatePendingFor(mandate);

        verify(mockedDirectDebitEventService).registerMandatePendingEventFor(mandate);
        assertThat(mandate.getState(), is(PENDING));
    }

    @Test
    public void shouldThrowWhenSubmittingTheSameDetailsSeveralTimes_andCreateOnlyOneEvent_whenConfirmingDirectDebitDetails() {
        Mandate mandate = MandateFixture
                .aMandateFixture()
                .withState(AWAITING_DIRECT_DEBIT_DETAILS)
                .toEntity();

        when(mockedMandateDao.findByExternalId(mandate.getExternalId())).thenReturn(Optional.of(mandate));
        // first call with confirmation details
        service.confirmedDirectDebitDetailsFor(mandate.getExternalId());

        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition DIRECT_DEBIT_DETAILS_CONFIRMED from state SUBMITTED is not valid");
        // second call with same details that should throw and prevent adding a new event
        Mandate newMandate = service.confirmedDirectDebitDetailsFor(mandate.getExternalId());

        assertThat(newMandate.getState(), is(SUBMITTED));
        verify(mockedDirectDebitEventService, times(1)).registerDirectDebitConfirmedEventFor(newMandate);
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromCreated() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.CREATED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromSubmitted() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.SUBMITTED)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test
    public void shouldSetMandateStatusToExpired_FromDdDetails() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
        verify(mockedDirectDebitEventService).registerMandateExpiredEventFor(mandate);
        verify(mockedMandateDao).updateState(mandate.getId(), MandateState.EXPIRED);
        assertThat(mandate.getState(), is(MandateState.EXPIRED));
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void shouldNotExpireMandateSinceWrongState_PENDING() {
        Mandate mandate = MandateFixture.aMandateFixture()
                .withState(MandateState.PENDING)
                .withCreatedDate(ZonedDateTime.now().minusMinutes(91L))
                .toEntity();
        service.mandateExpiredFor(mandate);
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
    public void confirm_shouldConfirmMandateByRegisteringExpectedEvents_whenNoTransactionIsSupplied() {
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678");

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS).withExternalId(mandateExternalId).toEntity();
        when(mockedMandateDao.findByExternalId(mandateExternalId)).thenReturn(Optional.of(mandate));
        ConfirmationDetails confirmationDetails = service.confirm(mandateExternalId, confirmMandateRequest);
        assertThat(confirmationDetails.getMandate(), is(mandate));
        assertThat(confirmationDetails.getSortCode(), is(SortCode.of("123456")));
        assertThat(confirmationDetails.getAccountNumber(), is("12345678"));
        assertThat(confirmationDetails.getTransaction(), is(nullValue()));
    }

    @Test
    public void confirm_shouldConfirmAPaymentByRegisteringExpectedEvents_whenATransactionIsSuppliedAndExists() {
        String transactionExternaId = "usdfhkdhfksd";
        ImmutableMap<String, String> confirmMandateRequest = ImmutableMap
                .of("sort_code", "123456", "account_number", "12345678", "transaction_external_id", transactionExternaId);

        String mandateExternalId = "test-mandate-ext-id";
        Mandate mandate = MandateFixture.aMandateFixture().withState(MandateState.AWAITING_DIRECT_DEBIT_DETAILS).withExternalId(mandateExternalId).toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
        when(mockedMandateDao.findByExternalId(mandateExternalId)).thenReturn(Optional.of(mandate));
        when(mockedTransactionService.findTransactionForExternalId(transactionExternaId)).thenReturn(transaction);
        ConfirmationDetails confirmationDetails = service.confirm(mandateExternalId, confirmMandateRequest);
        assertThat(confirmationDetails.getMandate(), is(mandate));
        assertThat(confirmationDetails.getSortCode(), is(SortCode.of("123456")));
        assertThat(confirmationDetails.getAccountNumber(), is("12345678"));
        assertThat(confirmationDetails.getTransaction(), is(transaction));
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

        return service.createMandate(getMandateRequestPayload(), gatewayAccount.getExternalId());
    }
}
