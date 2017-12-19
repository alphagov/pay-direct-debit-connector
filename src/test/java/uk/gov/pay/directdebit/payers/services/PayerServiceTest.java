package uk.gov.pay.directdebit.payers.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {
    @Mock
    private PayerDao mockedPayerDao;

    @Mock
    private TransactionService mockedTransactionService;

    @Mock
    private PayerParser mockedPayerParser;

    private PayerService service;
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture();

    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
            .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withPaymentRequestId(paymentRequestFixture.getId());

    private Map<String, String> createPaymentRequest = new HashMap<>();
    @Before
    public void setUp() throws Exception {
        service = new PayerService(mockedPayerDao, mockedTransactionService, mockedPayerParser);
    }

    @Test
    public void shouldStoreAPayerAndRelativeEvents() {
        Payer parsedPayer = PayerFixture.aPayerFixture().toEntity();
        Transaction transaction = transactionFixture.toEntity();
        when(mockedTransactionService.receiveDirectDebitDetailsFor(paymentRequestFixture.getExternalId()))
                .thenReturn(transaction);
        when(mockedPayerParser.parse(createPaymentRequest, transaction)).thenReturn(parsedPayer);
        service.create(paymentRequestFixture.getExternalId(), createPaymentRequest);
        verify(mockedPayerDao).insert(parsedPayer);
        verify(mockedTransactionService).payerCreatedFor(transactionFixture.toEntity());
    }
}
