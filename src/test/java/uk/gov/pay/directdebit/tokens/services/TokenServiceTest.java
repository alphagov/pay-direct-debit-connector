package uk.gov.pay.directdebit.tokens.services;

import org.exparity.hamcrest.date.ZonedDateTimeMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;
import uk.gov.pay.directdebit.payments.services.TransactionService;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TokenDao mockedTokenDao;

    @Mock
    private TransactionService mockedTransactionService;

    @Mock
    private PaymentRequestEventService mockedPaymentRequestEventService;

    private TokenService service;
    TransactionFixture transactionFixture = aTransactionFixture().withState(PaymentState.NEW);
    String token = "token";

    @Before
    public void setUp() throws Exception {
        service = new TokenService(mockedTokenDao, mockedTransactionService);
        when(mockedTransactionService.findChargeForToken(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
    }

    @Test
    public void shouldGenerateANewToken() {
        PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture();
        Token token = service.generateNewTokenFor(paymentRequestFixture.toEntity());
        assertThat(token.getId(), is(notNullValue()));
        assertThat(token.getToken(), is(notNullValue()));
        assertThat(token.getPaymentRequestId(), is(paymentRequestFixture.getId()));
    }

    @Test
    public void shouldValidateAPaymentRequestWithAToken() {
        Transaction charge = service.validateChargeWithToken(token);
        assertThat(charge.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
    }

    @Test
    public void shouldThrowIfTokenDoesNotExist() {
        when(mockedTransactionService.findChargeForToken("not-existing")).thenReturn(Optional.empty());
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("No one-time token found for payment request");
        thrown.reportMissingExceptionWithMessage("TokenNotFoundException.class expected");
        service.validateChargeWithToken("not-existing");
    }

}
