package uk.gov.pay.directdebit.payments.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceTest {

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
    private String paymentRequestExternalId = "sdkfhsdkjfhjdks";

    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        service = new GoCardlessService(mockedGoCardlessClientWrapper, mockedGoCardlessCustomerDao);
        goCardlessCustomer = new GoCardlessCustomer(null, payer.getId(), CUSTOMER_ID, BANK_ACCOUNT_ID);
    }

    @Test
    public void shouldCreateAGocardlessCustomerAndBankAccountForValidPaymentRequestAndPayer() {
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer))
                .thenReturn(goCardlessCustomer);
        when(mockedGoCardlessCustomerDao.insert(goCardlessCustomer)).thenReturn(40L);
        when(mockedGoCardlessClientWrapper.createCustomerBankAccount(paymentRequestExternalId, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenReturn(goCardlessCustomer);

        service.createCustomer(paymentRequestExternalId, payer, SORT_CODE, ACCOUNT_NUMBER);
        verify(mockedGoCardlessCustomerDao).updateBankAccountId(goCardlessCustomer.getId(), goCardlessCustomer.getCustomerBankAccountId());


        assertThat(goCardlessCustomer.getId(), is(40L));
        assertThat(goCardlessCustomer.getPayerId(), is(payer.getId()));
        assertThat(goCardlessCustomer.getCustomerId(), is(CUSTOMER_ID));
        assertThat(goCardlessCustomer.getCustomerBankAccountId(), is(BANK_ACCOUNT_ID));
    }
    @Test
    public void shouldThrow_ifFailingToCreateCustomerInGoCardless()  {
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer))
                .thenThrow(new RuntimeException("ooops"));
        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(String.format("Failed to create customer in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerFailedException expected");
        service.createCustomer(paymentRequestExternalId, payer, SORT_CODE, ACCOUNT_NUMBER);
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
        service.createCustomer(paymentRequestExternalId, payer, SORT_CODE, ACCOUNT_NUMBER);
    }
}
