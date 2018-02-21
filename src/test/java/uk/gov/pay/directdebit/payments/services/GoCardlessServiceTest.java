package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.TransactionFixture.aTransactionFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceTest {

    @Mock
    PayerService mockedPayerService;
    @Mock
    GoCardlessClientWrapper mockedGoCardlessClientWrapper;
    @Mock
    GoCardlessCustomerDao mockedGoCardlessCustomerDao;


    GoCardlessCustomer goCardlessCustomer;

    private GoCardlessService service;

    private final String SORT_CODE = "123456";
    private final String ACCOUNT_NUMBER = "12345678";
    private final String CUSTOMER_ID = "CU328471";
    private final String BANK_ACCOUNT_ID = "BA34983496";
    private final Map<String, String> createPayerRequest = ImmutableMap.of(
            "sort_code", SORT_CODE,
            "account_number", ACCOUNT_NUMBER
    );
    private String paymentRequestExternalId = "sdkfhsdkjfhjdks";

    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();

    private Transaction transaction = aTransactionFixture().toEntity();
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        service = new GoCardlessService(mockedPayerService, mockedGoCardlessClientWrapper, mockedGoCardlessCustomerDao);
        goCardlessCustomer = new GoCardlessCustomer(null, payer.getId(), CUSTOMER_ID, BANK_ACCOUNT_ID);
        when(mockedPayerService.create(paymentRequestExternalId, gatewayAccount.getId(), createPayerRequest)).thenReturn(payer);
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer)).thenReturn(goCardlessCustomer);
    }

    @Test
    public void shouldStoreAPayerAndGocardlessCustomerWhenReceivingCreatePayerRequest() {
        when(mockedGoCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
        verify(mockedGoCardlessCustomerDao).insert(goCardlessCustomer);
        verify(mockedGoCardlessClientWrapper).createCustomer(paymentRequestExternalId, payer);
        verify(mockedGoCardlessClientWrapper).createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER);
    }

    @Test
    public void shouldThrow_ifFailingToCreateCustomerInGoCardless()  {
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer))
                .thenThrow(new RuntimeException("ooops"));
        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(String.format("Failed to create customer in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerFailedException expected");
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
    }

    @Test
    public void shouldThrow_ifFailingToCreateCustomerBankAccountInGoCardless()  {
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer))
                .thenReturn(goCardlessCustomer);
        when(mockedGoCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenThrow(new RuntimeException("oops"));
        thrown.expect(CreateCustomerBankAccountFailedException.class);
        thrown.expectMessage(String.format("Failed to create customer bank account in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
    }
}
