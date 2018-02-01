package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.pay.directdebit.app.config.LinksConfig;
import uk.gov.pay.directdebit.gatewayaccounts.dao.GatewayAccountDao;
import uk.gov.pay.directdebit.gatewayaccounts.exception.GatewayAccountNotFoundException;
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.exception.PaymentRequestNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequest;
import uk.gov.pay.directdebit.payments.model.PaymentState;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture.aPaymentRequestFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentRequestServiceTest {
    private static final String SERVICE_HOST = "http://my-service";
    private static final long GATEWAY_ACCOUNT_ID = 1L;
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
    private PaymentRequestFixture paymentRequest = aPaymentRequestFixture()
            .withAmount(Long.parseLong(AMOUNT))
            .withDescription(DESCRIPTION)
            .withReference(REFERENCE)
            .withReturnUrl(RETURN_URL)
            .withGatewayAccountId(GATEWAY_ACCOUNT_ID);
    private TransactionFixture transaction = aTransactionFixture()
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

        when(mockTransactionService.createChargeFor(Mockito.any(PaymentRequest.class))).thenReturn(transaction.toEntity());

        when(mockedTokenService.generateNewTokenFor(Mockito.any(PaymentRequest.class))).thenReturn(token.toEntity());

        service = new PaymentRequestService(mockedConfig, mockedPaymentRequestDao, mockedTokenService, mockTransactionService, mockGatewayAccountDao);
        when(mockGatewayAccountDao.findById(GATEWAY_ACCOUNT_ID)).thenReturn(
                Optional.of(aGatewayAccountFixture().toEntity()));
    }

    @Test
    public void serviceCreate_shouldCreateAPaymentRequest() throws Exception {
        Long amount = 100L;

        service.createCharge(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());

        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        assertThat(createdPaymentRequest.getGatewayAccountId(), is(GATEWAY_ACCOUNT_ID));
        assertThat(createdPaymentRequest.getExternalId(), is(notNullValue()));
        assertThat(createdPaymentRequest.getReference(), is("Pay reference"));
        assertThat(createdPaymentRequest.getDescription(), is("This is a description"));
        assertThat(createdPaymentRequest.getAmount(), is(amount));
        assertThat(createdPaymentRequest.getReturnUrl(), is("http://return-service.com"));
        assertThat(createdPaymentRequest.getCreatedDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS, ZonedDateTime.now())));
    }

    @Test
    public void serviceCreate_shouldCreateATransaction() throws Exception {
        service.createCharge(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);
        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());
        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        verify(mockTransactionService).createChargeFor(createdPaymentRequest);
    }

    @Test
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndTransactionIsInProgress() throws URISyntaxException {
        when(mockedPaymentRequestDao.findByExternalId(paymentRequest.getExternalId()))
                .thenReturn(Optional.of(paymentRequest.toEntity()));
        when(mockTransactionService.findChargeForExternalId(paymentRequest.getExternalId()))
                .thenReturn(transaction.toEntity());
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(paymentRequest.getExternalId(), uriInfo);
        verify(mockedTokenService).generateNewTokenFor(paymentRequest.toEntity());

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(paymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/1/charges/" + paymentRequest.getExternalId())),
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
    public void serviceCreate_shouldThrowIfGatewayAccountDoesNotExist() throws Exception {
        when(mockGatewayAccountDao.findById(GATEWAY_ACCOUNT_ID)).thenReturn(
                Optional.empty());

        thrown.expect(GatewayAccountNotFoundException.class);
        thrown.expectMessage("Unknown gateway account: 1");
        thrown.reportMissingExceptionWithMessage("GatewayAccountNotFoundException expected");
        service.createCharge(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

    }

    @Test
    @Ignore("Not final states defined yet")
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndChargeIsInFinalState() throws URISyntaxException {
        transaction.withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        when(mockedPaymentRequestDao.findByExternalId(paymentRequest.getExternalId())).thenReturn(Optional.of(paymentRequest.toEntity()));
        when(mockTransactionService.findChargeForExternalId(paymentRequest.getExternalId()))
                .thenReturn(transaction.toEntity());
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(paymentRequest.getExternalId(), uriInfo);
        verifyNoMoreInteractions(mockedTokenService);

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(paymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/1/charges/" + paymentRequest.getExternalId()))
        ));
    }

    @Test
    public void getPaymentWithExternalId_shouldThrow_ifPaymentDoesNotExist() throws URISyntaxException {
        String externalPaymentId = "not_existing";
        when(mockedPaymentRequestDao.findByExternalId(externalPaymentId)).thenReturn(Optional.empty());
        thrown.expect(PaymentRequestNotFoundException.class);
        thrown.expectMessage("No payment request found with id: " + externalPaymentId);
        thrown.reportMissingExceptionWithMessage("PaymentNotFoundException expected");
        service.getPaymentWithExternalId(externalPaymentId, uriInfo);
    }
}
