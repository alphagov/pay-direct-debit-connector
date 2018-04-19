package uk.gov.pay.directdebit.payers.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.mandate.exception.PayerNotFoundException;
import uk.gov.pay.directdebit.payers.api.PayerParser;
import uk.gov.pay.directdebit.payers.dao.PayerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.payments.services.TransactionService;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class PayerServiceTest {

    @Mock
    PayerDao mockedPayerDao;
    @Mock
    TransactionService mockedTransactionService;
    @Mock
    PayerParser mockedPayerParser;

    private PayerService service;

    private final String SORT_CODE = "123456";
    private final String ACCOUNT_NUMBER = "12345678";
    private final Map<String, String> createPayerRequest = ImmutableMap.of(
            "sort_code", SORT_CODE,
            "account_number", ACCOUNT_NUMBER
    );
    private String paymentRequestExternalId = "sdkfhsdkjfhjdks";

    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();

    private Transaction transaction = aTransactionFixture().withPaymentRequestExternalId(paymentRequestExternalId).toEntity();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        service = new PayerService(mockedPayerDao, mockedTransactionService, mockedPayerParser);

        when(mockedTransactionService.receiveDirectDebitDetailsFor(gatewayAccount.getExternalId(), paymentRequestExternalId)).thenReturn(transaction);
        when(mockedPayerParser.parse(createPayerRequest, transaction)).thenReturn(payer);
    }

    @Test
    public void shouldStoreAPayerWhenReceivingCreatePayerRequest() {
        service.create(paymentRequestExternalId, gatewayAccount.getExternalId(), createPayerRequest);
        verify(mockedPayerDao).insert(payer);
        verify(mockedTransactionService).payerCreatedFor(transaction);
    }

    @Test
    public void shouldReturnPayerForTransactionIfItExists() {
        when(mockedPayerDao.findByPaymentRequestId(transaction.getPaymentRequest().getId())).thenReturn(Optional.of(payer));
        Payer payer = service.getPayerFor(transaction);
        assertThat(payer.getId(), is(payer.getId()));
        assertThat(payer.getExternalId(), is(payer.getExternalId()));
    }

    @Test
    public void shouldThrowIfGatewayAccountDoesNotExist() {
        when(mockedPayerDao.findByPaymentRequestId(transaction.getPaymentRequest().getId())).thenReturn(Optional.empty());
        thrown.expect(PayerNotFoundException.class);
        thrown.expectMessage("Couldn't find payer for payment request with external id: sdkfhsdkjfhjdks");
        thrown.reportMissingExceptionWithMessage("PayerNotFoundException expected");
        service.getPayerFor(transaction);
    }
}
