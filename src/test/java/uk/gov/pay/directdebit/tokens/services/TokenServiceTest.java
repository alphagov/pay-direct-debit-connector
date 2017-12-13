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
import uk.gov.pay.directdebit.payments.dao.PaymentRequestEventDao;
import uk.gov.pay.directdebit.payments.dao.TransactionDao;
import uk.gov.pay.directdebit.payments.exception.InvalidStateTransitionException;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.tokens.dao.TokenDao;
import uk.gov.pay.directdebit.tokens.exception.TokenNotFoundException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private TokenDao mockedTokenDao;

    @Mock
    private TransactionDao mockedTransactionDao;

    @Mock
    private PaymentRequestEventDao mockedPaymentRequestEventDao;

    private TokenService service;

    @Before
    public void setUp() throws Exception {
        service = new TokenService(mockedTokenDao, mockedPaymentRequestEventDao, mockedTransactionDao);
    }

    @Test
    public void shouldValidateAPaymentRequestWithAToken() {
        TransactionFixture transactionFixture = aTransactionFixture().withState(PaymentState.NEW);
        String token = "token";
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        Transaction charge = service.validateChargeWithToken(token);
        assertThat(charge.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(charge.getState(), is(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS));
    }

    @Test
    public void shouldCreateATokenExchangedEvent_whenTokenIsValid() throws Exception{
        TransactionFixture transactionFixture = aTransactionFixture();
        String token = "token";
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        service.validateChargeWithToken(token);

        ArgumentCaptor<PaymentRequestEvent> paymentRequestEventArgumentCaptor = forClass(PaymentRequestEvent.class);
        verify(mockedPaymentRequestEventDao).insert(paymentRequestEventArgumentCaptor.capture());
        PaymentRequestEvent createdPaymentRequestEvent = paymentRequestEventArgumentCaptor.getValue();
        assertThat(createdPaymentRequestEvent.getPaymentRequestId(), is(transactionFixture.getPaymentRequestId()));
        assertThat(createdPaymentRequestEvent.getEventType(), is(PaymentRequestEvent.Type.CHARGE));
        assertThat(createdPaymentRequestEvent.getEvent(), is(PaymentRequestEvent.SupportedEvent.TOKEN_EXCHANGED));
        assertThat(createdPaymentRequestEvent.getEventDate(),
                is(ZonedDateTimeMatchers.within(10, ChronoUnit.SECONDS,  ZonedDateTime.now())));
    }

    @Test
    public void shouldThrowIfTokenDoesNotExist() {
        thrown.expect(TokenNotFoundException.class);
        thrown.expectMessage("No one-time token found for payment request");
        thrown.reportMissingExceptionWithMessage("TokenNotFoundException.class expected");
        service.validateChargeWithToken("not-existing");
    }

    @Test
    public void shouldThrowIfChargeIsInInvalidState() {
        thrown.expect(InvalidStateTransitionException.class);
        thrown.expectMessage("Transition TOKEN_EXCHANGED from state AWAITING_DIRECT_DEBIT_DETAILS is not valid");
        thrown.reportMissingExceptionWithMessage("InvalidStateTransitionException.class expected");
        TransactionFixture transactionFixture = aTransactionFixture().withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS);
        String token = "token";
        when(mockedTransactionDao.findByTokenId(token))
                .thenReturn(Optional.of(transactionFixture.toEntity()));
        service.validateChargeWithToken(token);
    }

}
