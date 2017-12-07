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
import uk.gov.pay.directdebit.payments.api.PaymentRequestResponse;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestDao;
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TokenDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.ChargeNotFoundException;
import uk.gov.pay.directdebit.payments.model.*;

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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.Type;

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

    @Mock
    private TokenDao mockedTokenDao;
    @Mock
    private DirectDebitConfig mockedConfig;
    @Mock
    private UriInfo mockedUriInfo;
    @Mock
    private LinksConfig mockedLinksConfig;
    @Mock
    private PaymentRequestDao mockedPaymentRequestDao;
    @Mock
    private PaymentRequestEventDao mockedPaymentRequestEventDao;
    @Mock
    private TransactionDao mockTransactionDao;
    @Mock
    private UriInfo uriInfo;

    private PaymentRequestService service;

    @Before
    public void setUp() throws Exception {

        when(mockedConfig.getLinks())
                .thenReturn(mockedLinksConfig);

        when(mockedLinksConfig.getFrontendUrl())
                .thenReturn("http://payments.com");

        doAnswer(invocation -> fromUri(SERVICE_HOST))
                .when(this.mockedUriInfo)
                .getBaseUriBuilder();

        service = new PaymentRequestService(mockedConfig, mockedPaymentRequestDao, mockedTokenDao, mockedPaymentRequestEventDao, mockTransactionDao);
    }

    @Test
    public void serviceCreate_shouldCreateAPaymentRequest() throws Exception{
        Long amount = 100L;
        service.create(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

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
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS,  ZonedDateTime.now())));
    }

    @Test
    public void serviceCreate_shouldCreateAPaymentRequestEvent() throws Exception{
        service.create(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());
        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        ArgumentCaptor<PaymentRequestEvent> paymentRequestEventArgumentCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(paymentRequestEventArgumentCaptor.capture());
        PaymentRequestEvent createdPaymentRequestEvent = paymentRequestEventArgumentCaptor.getValue();
        assertThat(createdPaymentRequestEvent.getPaymentRequestId(), is(createdPaymentRequest.getId()));
        assertThat(createdPaymentRequestEvent.getEventType(), is(Type.CHARGE));
        assertThat(createdPaymentRequestEvent.getEvent(), is(SupportedEvent.CHARGE_CREATED));
        assertThat(createdPaymentRequestEvent.getEventDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS,  ZonedDateTime.now())));
    }

    @Test
    public void serviceCreate_shouldCreateATransaction() throws Exception{
        Long amount = 100L;
        service.create(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());
        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();

        ArgumentCaptor<Transaction> transactionArgumentCaptor = forClass(Transaction.class);
        verify(mockTransactionDao).insert(transactionArgumentCaptor.capture());
        Transaction createdTransaction = transactionArgumentCaptor.getValue();
        assertThat(createdTransaction.getPaymentRequestId(), is(createdPaymentRequest.getId()));
        assertThat(createdTransaction.getAmount(), is(amount));
        assertThat(createdTransaction.getType(), is(Transaction.Type.CHARGE));
        assertThat(createdTransaction.getState(), is(PaymentState.NEW));
    }

    //todo needs proper checking of values when we introduce the gateway accounts dao
    public void serviceCreate_shouldCreateAToken() throws Exception{
        service.create(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);

        ArgumentCaptor<Token> tokenEntityArgumentCaptor = forClass(Token.class);
        verify(mockedTokenDao).insert(tokenEntityArgumentCaptor.capture());

        Token tokenEntity = tokenEntityArgumentCaptor.getValue();
        assertThat(tokenEntity.getPaymentRequestId(), is(notNullValue()));
        assertThat(tokenEntity.getToken(), is(notNullValue()));
    }

    @Test
    public void serviceCreate_shouldPopulateAResponse() throws Exception{
        PaymentRequestResponse response = service.create(CHARGE_REQUEST, GATEWAY_ACCOUNT_ID, mockedUriInfo);
        ArgumentCaptor<PaymentRequest> paymentRequestArgumentCaptor = forClass(PaymentRequest.class);
        verify(mockedPaymentRequestDao).insert(paymentRequestArgumentCaptor.capture());

        ArgumentCaptor<Token> tokenEntityArgumentCaptor = forClass(Token.class);
        verify(mockedTokenDao).insert(tokenEntityArgumentCaptor.capture());

        PaymentRequest createdPaymentRequest = paymentRequestArgumentCaptor.getValue();
        Token createdToken = tokenEntityArgumentCaptor.getValue();

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(createdPaymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/1/charges/" + createdPaymentRequest.getExternalId())),
                ImmutableMap.of("rel", "next_url", "method", "GET", "href", new URI("http://payments.com/secure/" + createdToken.getToken())),
                ImmutableMap.<String, Object>builder()
                        .put("rel", "next_url_post")
                        .put("method", "POST")
                        .put("href", new URI("http://payments.com/secure"))
                        .put("type","application/x-www-form-urlencoded")
                        .put("params", new HashMap<String, Object>() {{
                    put("chargeTokenId", createdToken.getToken());
                }}).build()
        ));
    }

    @Test
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndTransactionIsInProgress() throws URISyntaxException {
        PaymentRequest paymentRequest = new PaymentRequest(Long.parseLong(AMOUNT), RETURN_URL, GATEWAY_ACCOUNT_ID, DESCRIPTION, REFERENCE);
        Transaction transaction = new Transaction(paymentRequest.getId(), 2L, Transaction.Type.CHARGE, PaymentState.NEW);
        when(mockedPaymentRequestDao.findByExternalId(paymentRequest.getExternalId())).thenReturn(Optional.of(paymentRequest));
        when(mockTransactionDao.findByPaymentRequestId(paymentRequest.getId())).thenReturn(Optional.of(transaction));
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(paymentRequest.getExternalId(), uriInfo);
        ArgumentCaptor<Token> tokenEntityArgumentCaptor = forClass(Token.class);
        verify(mockedTokenDao).insert(tokenEntityArgumentCaptor.capture());
        Token createdToken = tokenEntityArgumentCaptor.getValue();

        assertThat(response.getAmount().toString(), is(AMOUNT));
        assertThat(response.getDescription(), is(DESCRIPTION));
        assertThat(response.getReference(), is(REFERENCE));
        assertThat(response.getReturnUrl(), is(RETURN_URL));
        assertThat(response.getPaymentExternalId(), is(paymentRequest.getExternalId()));
        assertThat(response.getDataLinks(), hasItems(
                ImmutableMap.of("rel", "self", "method", "GET", "href", new URI(SERVICE_HOST + "/v1/api/accounts/1/charges/" + paymentRequest.getExternalId())),
                ImmutableMap.of("rel", "next_url", "method", "GET", "href", new URI("http://payments.com/secure/" + createdToken.getToken())),
                ImmutableMap.<String, Object>builder()
                        .put("rel", "next_url_post")
                        .put("method", "POST")
                        .put("href", new URI("http://payments.com/secure"))
                        .put("type","application/x-www-form-urlencoded")
                        .put("params", new HashMap<String, Object>() {{
                            put("chargeTokenId", createdToken.getToken());
                        }}).build()
        ));
    }

    @Test
    public void getPaymentWithExternalId_shouldPopulateAResponse_ifPaymentExistsAndChargeIsInFinalState() throws URISyntaxException {
        PaymentRequest paymentRequest = new PaymentRequest(Long.parseLong(AMOUNT), RETURN_URL, GATEWAY_ACCOUNT_ID, DESCRIPTION, REFERENCE);
        Transaction transaction = new Transaction(paymentRequest.getId(), 2L, Transaction.Type.CHARGE, PaymentState.REQUESTED_FAILED);
        when(mockedPaymentRequestDao.findByExternalId(paymentRequest.getExternalId())).thenReturn(Optional.of(paymentRequest));
        when(mockTransactionDao.findByPaymentRequestId(paymentRequest.getId())).thenReturn(Optional.of(transaction));
        when(uriInfo.getBaseUriBuilder())
                .thenReturn(UriBuilder.fromUri(SERVICE_HOST));


        PaymentRequestResponse response = service.getPaymentWithExternalId(paymentRequest.getExternalId(), uriInfo);
        verifyNoMoreInteractions(mockedTokenDao);

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
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment request with id: " + externalPaymentId);
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.getPaymentWithExternalId(externalPaymentId, uriInfo);
    }

    @Test
    public void getPaymentWithExternalId_shouldThrow_ifNoChargeExistsForPayment() throws URISyntaxException {
        PaymentRequest paymentRequest = new PaymentRequest(Long.parseLong(AMOUNT), RETURN_URL, GATEWAY_ACCOUNT_ID, DESCRIPTION, REFERENCE);
        when(mockedPaymentRequestDao.findByExternalId(paymentRequest.getExternalId())).thenReturn(Optional.of(paymentRequest));
        thrown.expect(ChargeNotFoundException.class);
        thrown.expectMessage("No charges found for payment request with id: " + paymentRequest.getExternalId());
        thrown.reportMissingExceptionWithMessage("ChargeNotFoundException expected");
        service.getPaymentWithExternalId(paymentRequest.getExternalId(), uriInfo);
    }
}
