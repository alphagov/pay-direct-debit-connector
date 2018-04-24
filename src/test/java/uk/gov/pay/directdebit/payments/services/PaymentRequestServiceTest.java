package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.fixtures.TokenFixture;
import uk.gov.pay.directdebit.tokens.services.TokenService;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestServiceTest {
    private static final String SERVICE_HOST = "http://my-service";
    private static final String GATEWAY_ACCOUNT_EXTERNAL_ID = "DIRECT_DEBIT:accountExternalId";
    private static final String AMOUNT = "100";
    private static final String RETURN_URL = "http://return-service.com";
    private static final String DESCRIPTION = "This is a description";
    private static final String REFERENCE = "Pay reference";
    private static final Map<String, String> CHARGE_REQUEST = new HashMap<String, String>() {{
        put("amount", AMOUNT);
        put("return_url", RETURN_URL);
        put("description", DESCRIPTION);
        put("reference", REFERENCE);
    }};
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private GatewayAccountFixture gatewayAccountFixture = aGatewayAccountFixture().withExternalId(GATEWAY_ACCOUNT_EXTERNAL_ID);
    private GatewayAccount gatewayAccount = gatewayAccountFixture.toEntity();
    private PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
            .withAmount(Long.parseLong(AMOUNT))
            .withDescription(DESCRIPTION)
            .withReference(REFERENCE)
            .withReturnUrl(RETURN_URL)
            .withGatewayAccountId(gatewayAccountFixture.getId());
    private TransactionFixture transactionFixture = aTransactionFixture()
            .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withPaymentRequestId(paymentRequest.getId())
            .withPaymentRequestExternalId(paymentRequest.getExternalId())
            .withAmount(paymentRequest.getAmount());

    private final TokenFixture token = TokenFixture.aTokenFixture();

    @Mock
    private TokenService mockedTokenService;
    @Mock
    private DirectDebitConfig mockedConfig;
    @Mock
    private UriInfo mockedUriInfo;
    @Mock
    private LinksConfig mockedLinksConfig;
    @Mock
    private PaymentRequestDao mockedPaymentRequestDao;
    @Mock
    private TransactionService mockTransactionService;
    @Mock
    private GatewayAccountDao mockGatewayAccountDao;
    @Mock
    private UriInfo uriInfo;

    private PaymentRequestService service;

    @Before
    public void setUp() {

        when(mockedConfig.getLinks())
                .thenReturn(mockedLinksConfig);

        when(mockedLinksConfig.getFrontendUrl())
                .thenReturn("http://payments.com");
        doAnswer(invocation -> fromUri(SERVICE_HOST))
                .when(this.mockedUriInfo)
                .getBaseUriBuilder();

        when(mockTransactionService.createChargeFor(any(PaymentRequest.class), any(GatewayAccount.class))).thenReturn(transactionFixture.toEntity());

        when(mockedTokenService.generateNewTokenFor(any(PaymentRequest.class))).thenReturn(token.toEntity());

        service = new PaymentRequestService(mockedConfig, mockedPaymentRequestDao, mockedTokenService, mockTransactionService, mockGatewayAccountDao);

        when(mockGatewayAccountDao.findByExternalId(GATEWAY_ACCOUNT_EXTERNAL_ID)).thenReturn(
                Optional.of(gatewayAccount));
    }

    @Test
    public void serviceCreate_shouldCreateAPaymentRequest() {
        Long amount = 100L;

        service.createTransaction(CHARGE_REQUEST, GATEWAY_ACCOUNT_EXTERNAL_ID, mockedUriInfo);

        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());

        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        assertThat(createdPaymentRequest.getGatewayAccountId(), is(gatewayAccountFixture.getId()));
        assertThat(createdPaymentRequest.getExternalId(), is(notNullValue()));
        assertThat(createdPaymentRequest.getReference(), is("Pay reference"));
        assertThat(createdPaymentRequest.getDescription(), is("This is a description"));
        assertThat(createdPaymentRequest.getAmount(), is(amount));
        assertThat(createdPaymentRequest.getReturnUrl(), is("http://return-service.com"));
        assertThat(createdPaymentRequest.getCreatedDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void serviceCreate_shouldCreateATransaction() {
        service.createTransaction(CHARGE_REQUEST, GATEWAY_ACCOUNT_EXTERNAL_ID, mockedUriInfo);
        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());
        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        verify(mockTransactionService).createChargeFor(createdPaymentRequest, gatewayAccount);
    }

    @Test
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndTransactionIsInProgress() throws URISyntaxException {
        when(mockedPaymentRequestDao.findByExternalIdAndAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(Optional.of(paymentRequest.toEntity()));
        when(mockTransactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(transactionFixture.toEntity());
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(gatewayAccountFixture.getExternalId(), paymentRequest.getExternalId(), uriInfo);
        verify(mockedTokenService).generateNewTokenFor(paymentRequest.toEntity());

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(paymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/DIRECT_DEBIT:accountExternalId/charges/" + paymentRequest.getExternalId())),
                ImmutableMap.of("rel", "next_url", "method", "GET", "href", new URI("http://payments.com/secure/" + token.getToken())),
                ImmutableMap.<String, Object>builder()
                        .put("rel", "next_url_post")
                        .put("method", "POST")
                        .put("href", new URI("http://payments.com/secure"))
                        .put("type", "application/x-www-form-urlencoded")
                        .put("params", new HashMap<String, Object>() {{
                            //backward-compatibility with demoservice
                            put("chargeTokenId", token.getToken());
                        }}).build()
        ));
    }

    @Test
    public void serviceCreate_shouldThrowIfGatewayAccountDoesNotExist() {
        when(mockGatewayAccountDao.findByExternalId(GATEWAY_ACCOUNT_EXTERNAL_ID)).thenReturn(
                Optional.empty());

        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: DIRECT_DEBIT:accountExternalId");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.createTransaction(CHARGE_REQUEST, GATEWAY_ACCOUNT_EXTERNAL_ID, mockedUriInfo);

    }

    @Test
    public void shouldDelegateToTheTransactionServiceToCancelACharge() {
        Transaction transaction = transactionFixture.toEntity();
        when(mockTransactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(transaction);

        service.cancelTransaction(gatewayAccountFixture.getExternalId(), paymentRequest.getExternalId());
        verify(mockTransactionService).paymentCancelledFor(transaction);
    }

    @Test
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndChargeIsInFinalState() throws URISyntaxException {
        when(mockedPaymentRequestDao.findByExternalIdAndAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId())).thenReturn(Optional.of(paymentRequest.toEntity()));
        when(mockTransactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(transactionFixture.toEntity());
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(gatewayAccountFixture.getExternalId(), paymentRequest.getExternalId(), uriInfo);
        verify(mockedTokenService).generateNewTokenFor(paymentRequest.toEntity());

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(paymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/DIRECT_DEBIT:accountExternalId/charges/" + paymentRequest.getExternalId()))
        ));
    }

    @Test
    public void getPaymentWithExternalId_shouldThrow_ifPaymentDoesNotExist()  {
        String externalPaymentId = "not_existing";
        when(mockedPaymentRequestDao.findByExternalIdAndAccountExternalId(externalPaymentId, gatewayAccountFixture.getExternalId())).thenReturn(Optional.empty());
        thrown.expect(PaymentRequestNotFoundException.class);
        thrown.expectMessage("No payment request found with id: " + externalPaymentId);
        thrown.reportMissingExceptionWithMessage("PaymentNotFoundException expected");
        service.getPaymentWithExternalId(gatewayAccountFixture.getExternalId(), externalPaymentId, uriInfo);
    }

    @Test
    public void shouldDelegateToTheTransactionServiceToChangeAPaymentMethod() {
        Transaction transaction = transactionFixture.toEntity();
        when(mockTransactionService.findTransactionForExternalIdAndGatewayAccountExternalId(paymentRequest.getExternalId(), gatewayAccountFixture.getExternalId()))
                .thenReturn(transaction);

        service.changePaymentMethod(gatewayAccountFixture.getExternalId(), paymentRequest.getExternalId());
        verify(mockTransactionService).paymentMethodChangedFor(transaction);
    }
}
