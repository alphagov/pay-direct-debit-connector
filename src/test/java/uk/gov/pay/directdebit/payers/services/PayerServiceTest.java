package uk.gov.pay.directdebit.payers.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.PaymentRequestFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentProviderMapper;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.SandboxService;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.GOCARDLESS;
import static uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider.SANDBOX;

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {
    @Mock
    private PayerDao mockedPayerDao;

    @Mock
    private TransactionService mockedTransactionService;

    @Mock
    private PayerParser mockedPayerParser;

    @Mock
    private PaymentProviderMapper mockedPaymentProviderMapper;

    @Mock
    private SandboxService sandboxService;


    private PayerService service;

    private GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture.aGatewayAccountFixture();
    private PaymentRequestFixture paymentRequestFixture = PaymentRequestFixture.aPaymentRequestFixture().withGatewayAccountId(gatewayAccountFixture.getId());
    private PayerFixture payerFixture = PayerFixture.aPayerFixture().withPaymentRequestId(paymentRequestFixture.getId());

    private TransactionFixture transactionFixture = TransactionFixture.aTransactionFixture()
            .withState(PaymentState.AWAITING_DIRECT_DEBIT_DETAILS)
            .withPaymentRequestId(paymentRequestFixture.getId());

    private Map<String, String> createPaymentRequest = new HashMap<>();
    @Before
    public void setUp() throws Exception {
        service = new PayerService(mockedPayerDao, mockedTransactionService, mockedPayerParser, mockedPaymentProviderMapper);
    }

    @Test
    public void shouldStoreAPayerAndRelativeEvents() {
        Payer parsedPayer = PayerFixture.aPayerFixture().toEntity();
        Transaction transaction = transactionFixture.toEntity();
        when(mockedTransactionService.receiveDirectDebitDetailsFor(gatewayAccountFixture.getId(), paymentRequestFixture.getExternalId()))
                .thenReturn(transaction);
        when(mockedPayerParser.parse(createPaymentRequest, transaction)).thenReturn(parsedPayer);
        service.create(gatewayAccountFixture.getId(), paymentRequestFixture.getExternalId(), createPaymentRequest);
        verify(mockedPayerDao).insert(parsedPayer);
        verify(mockedTransactionService).payerCreatedFor(transactionFixture.toEntity());
    }

    @Test
    public void shouldCallPaymentProviderToCreateACustomer() {
        String sortCode = "123456";
        String accountNumber = "12345678";
        Payer payer = PayerFixture.aPayerFixture().toEntity();
        Transaction transaction = transactionFixture.withPaymentProvider(GOCARDLESS).toEntity();
        when(mockedTransactionService.findChargeForExternalIdAndGatewayAccountId(paymentRequestFixture.getExternalId(), gatewayAccountFixture.getId()))
                .thenReturn(transaction);
        when(mockedPaymentProviderMapper.getServiceFor(GOCARDLESS)).thenReturn(sandboxService);
        service.createCustomerFor(gatewayAccountFixture.getId(), paymentRequestFixture.getExternalId(), payer, sortCode, accountNumber);
        verify(sandboxService).createCustomer(paymentRequestFixture.getExternalId(), payer, sortCode, accountNumber);
    }
}
