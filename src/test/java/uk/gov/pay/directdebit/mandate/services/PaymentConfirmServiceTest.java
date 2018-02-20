package uk.gov.pay.directdebit.mandate.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.dao.MandateDao;
import uk.gov.pay.directdebit.mandate.exception.PayerConflictException;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Optional;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PaymentConfirmServiceTest {

    private PaymentConfirmService service;

    @Mock
    private MandateDao mockMandateDao;
    @Mock
    private TransactionService mockTransactionService;
    @Mock
    private PayerDao mockPayerDao;

    @Before
    public void setup() {
        service = new PaymentConfirmService(mockTransactionService, mockPayerDao, mockMandateDao);
    }

    @Test
    public void confirm_shouldConfirmAPaymentByCreatingAMandateAndRegisteringExpectedEvents() {

        String paymentRequestExternalId = "test-payment-ext-id";
        long paymentRequestId = 1L;
        Long payerId = 2L;
        Long accountId = 6L;
        Long mandateId = 3L;

        Transaction transaction = aTransactionFixture().withPaymentRequestId(paymentRequestId).toEntity();
        when(mockTransactionService.confirmedDirectDebitDetailsFor(accountId, paymentRequestExternalId))
                .thenReturn(transaction);

        when(mockPayerDao.findByPaymentRequestId(paymentRequestId))
                .thenReturn(Optional.of(PayerFixture.aPayerFixture()
                        .withId(payerId).toEntity()));

        when(mockMandateDao.insert(any(Mandate.class))).thenReturn(mandateId);

        service.confirm(accountId, paymentRequestExternalId);

        verify(mockMandateDao)
                .insert(argThat(mandate -> mandate.getId() == null &&
                        mandate.getPayerId().equals(payerId) &&
                        mandate.getExternalId() != null));

        verify(mockTransactionService).mandateCreatedFor(transaction);
    }

    @Test
    public void confirm_shouldFail_whenPaymentDoesNotHaveAPayer() {

        String paymentRequestExternalId = "test-payment-ext-id";
        long paymentRequestId = 1L;
        long accountId = 2L;

        Transaction transaction = aTransactionFixture().withPaymentRequestId(paymentRequestId).toEntity();
        when(mockTransactionService.confirmedDirectDebitDetailsFor(accountId, paymentRequestExternalId))
                .thenReturn(transaction);

        when(mockPayerDao.findByPaymentRequestId(paymentRequestId))
                .thenReturn(Optional.empty());

        try {
            service.confirm(accountId, paymentRequestExternalId);
            fail("Expected PayerConflictException to be thrown");
        } catch (PayerConflictException e) {
            verify(mockMandateDao, never()).insert(any(Mandate.class));
            verify(mockTransactionService, never()).mandateCreatedFor(any(Transaction.class));
        }
    }
}
