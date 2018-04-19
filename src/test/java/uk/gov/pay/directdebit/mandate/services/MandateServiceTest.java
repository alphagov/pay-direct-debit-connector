package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.MandateNotFoundException;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.notifications.services.UserNotificationService;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestEventFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentRequestEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.PaymentRequestEventService;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.model.PaymentRequestEvent.SupportedEvent.MANDATE_PENDING;
import static uk.gov.pay.directdebit.payments.model.PaymentState.PENDING_DIRECT_DEBIT_PAYMENT;

@RunWith(MockitoJUnitRunner.class)
public class MandateServiceTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private PaymentRequestEventService mockedPaymentRequestEventService;
    @Mock
    private MandateDao mockedMandateDao;
    @Mock
    private UserNotificationService mockedUserNotificationService;

    private Payer payer = PayerFixture.aPayerFixture().toEntity();
    private Mandate mandate = MandateFixture.aMandateFixture()
            .withPayerId(payer.getId())
            .toEntity();


    private MandateService service;

    @Before
    public void setUp() {
        service = new MandateService(mockedMandateDao, mockedPaymentRequestEventService, mockedUserNotificationService);
    }


    @Test
    public void shouldUpdateTransactionStateRegisterEventAndSendEmail_whenMandateFails()  {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.of(mandate));
        service.mandateFailedFor(transaction, payer);
        verify(mockedPaymentRequestEventService).registerMandateFailedEventFor(transaction);
        verify(mockedUserNotificationService).sendMandateFailedEmailFor(transaction, mandate, payer);
    }

    @Test
    public void shouldUpdateTransactionStateRegisterEventAndSendEmail_whenMandateIsCancelled() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.of(mandate));
        service.mandateCancelledFor(transaction, payer);
        verify(mockedPaymentRequestEventService).registerMandateCancelledEventFor(transaction);
        verify(mockedUserNotificationService).sendMandateCancelledEmailFor(transaction, mandate, payer);
    }

    @Test
    public void shouldThrow_whenCantFindMandateForTransaction() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT).toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.empty());
        thrown.expect(MandateNotFoundException.class);
        thrown.expectMessage("Couldn't find mandate for transaction with id: " + transaction.getId());
        thrown.reportMissingExceptionWithMessage("MandateNotFoundException expected");
        service.mandateCancelledFor(transaction, payer);
    }


    @Test
    public void mandatePendingFor_shouldRegisterAMandatePendingEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        service.mandatePendingFor(transaction);

        verify(mockedPaymentRequestEventService).registerMandatePendingEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }

    @Test
    public void mandateActiveFor_shouldRegisterAMandateActiveEvent() {
        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();
        when(mockedMandateDao.findByTransactionId(transaction.getId())).thenReturn(Optional.of(mandate));
        service.mandateActiveFor(transaction);

        verify(mockedPaymentRequestEventService).registerMandateActiveEventFor(transaction);
        assertThat(transaction.getState(), is(PENDING_DIRECT_DEBIT_PAYMENT));
    }


    @Test
    public void findMandatePendingEventFor_shouldFindEvent() {

        Transaction transaction = TransactionFixture
                .aTransactionFixture()
                .withState(PENDING_DIRECT_DEBIT_PAYMENT)
                .toEntity();

        PaymentRequestEvent event = PaymentRequestEventFixture.aPaymentRequestEventFixture().toEntity();

        when(mockedPaymentRequestEventService.findBy(transaction.getPaymentRequest().getId(), PaymentRequestEvent.Type.MANDATE, MANDATE_PENDING))
                .thenReturn(Optional.of(event));

        PaymentRequestEvent foundEvent = service.findMandatePendingEventFor(transaction).get();

        assertThat(foundEvent, is(event));
    }
}
