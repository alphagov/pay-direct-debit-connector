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
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.services.PaymentConfirmService;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.services.PayerService;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;
import uk.gov.pay.directdebit.payments.dao.GoCardlessEventDao;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessMandateNotFoundException;
import uk.gov.pay.directdebit.payments.exception.GoCardlessPaymentNotFoundException;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture.aGoCardlessMandateFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture.aGatewayAccountFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceTest {

    private static final String SORT_CODE = "123456";
    private static final String ACCOUNT_NUMBER = "12345678";
    private static final String CUSTOMER_ID = "CU328471";
    private static final String BANK_ACCOUNT_ID = "BA34983496";
    private static final String PAYMENT_REQUEST_EXTERNAL_ID = "sdkfhsdkjfhjdks";
    private static final Map<String, String> CREATE_PAYER_REQUEST = ImmutableMap.of(
            "sort_code", SORT_CODE,
            "account_number", ACCOUNT_NUMBER
    );

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private PayerService mockedPayerService;
    @Mock
    private TransactionService mockedTransactionService;
    @Mock
    private GoCardlessClientWrapper mockedGoCardlessClientWrapper;
    @Mock
    private GoCardlessCustomerDao mockedGoCardlessCustomerDao;
    @Mock
    private GoCardlessMandateDao mockedGoCardlessMandateDao;
    @Mock
    private GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    @Mock
    private PaymentConfirmService mockedPaymentConfirmService;
    @Mock
    private GoCardlessEventDao mockedGoCardlessEventDao;
    private GoCardlessCustomer goCardlessCustomer;
    private GoCardlessService service;
    private Payer payer = PayerFixture.aPayerFixture()
            .withName("mr payment").toEntity();
    private Mandate mandate = MandateFixture.aMandateFixture().withPayerId(payer.getId()).toEntity();
    private Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
    private GoCardlessMandate goCardlessMandate =
            aGoCardlessMandateFixture()
                    .withMandateId(mandate.getId()).toEntity();
    private GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();
    private ConfirmationDetails confirmationDetails = new ConfirmationDetails(transaction, mandate);

    @Before
    public void setUp() {
        service = new GoCardlessService(mockedPayerService, mockedTransactionService, mockedPaymentConfirmService, mockedGoCardlessClientWrapper, mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao, mockedGoCardlessEventDao);
        goCardlessCustomer = new GoCardlessCustomer(null, payer.getId(), CUSTOMER_ID, BANK_ACCOUNT_ID);
        when(mockedPayerService.create(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount.getId(), CREATE_PAYER_REQUEST)).thenReturn(payer);
        when(mockedGoCardlessClientWrapper.createCustomer(PAYMENT_REQUEST_EXTERNAL_ID, payer)).thenReturn(goCardlessCustomer);

    }

    @Test
    public void createPayer_shouldStoreAPayerAndGocardlessCustomerWhenReceivingCreatePayerRequest() {
        when(mockedGoCardlessClientWrapper.createCustomerBankAccount(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
        service.createPayer(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount, CREATE_PAYER_REQUEST);
        verify(mockedGoCardlessCustomerDao).insert(goCardlessCustomer);
        verify(mockedGoCardlessClientWrapper).createCustomer(PAYMENT_REQUEST_EXTERNAL_ID, payer);
        verify(mockedGoCardlessClientWrapper).createCustomerBankAccount(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER);
    }

    @Test
    public void storeEvent_shouldStoreAGoCardlessEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        service.storeEvent(goCardlessEvent);
        verify(mockedGoCardlessEventDao).insert(goCardlessEvent);
    }

    @Test
    public void findPaymentForEvent_shouldThrowIfNoPaymentIsFoundForEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        String resourceId = "aaa";
        goCardlessEvent.setResourceId(resourceId);
        when(mockedGoCardlessPaymentDao.findByEventResourceId(resourceId)).thenReturn(Optional.empty());
        thrown.expect(GoCardlessPaymentNotFoundException.class);
        thrown.expectMessage("No gocardless payment found with resource id: aaa");
        thrown.reportMissingExceptionWithMessage("GoCardlessPaymentNotFoundException expected");
        service.findPaymentForEvent(goCardlessEvent);
    }

    @Test
    public void findMandateForEvent_shouldThrowIfNoMandateIsFoundForEvent() {
        GoCardlessEvent goCardlessEvent = aGoCardlessEventFixture().toEntity();
        String resourceId = "aaa";
        goCardlessEvent.setResourceId(resourceId);
        when(mockedGoCardlessMandateDao.findByEventResourceId(resourceId)).thenReturn(Optional.empty());
        thrown.expect(GoCardlessMandateNotFoundException.class);
        thrown.expectMessage("No gocardless mandate found with resource id: aaa");
        thrown.reportMissingExceptionWithMessage("GoCardlessMandateNotFoundException expected");
        service.findMandateForEvent(goCardlessEvent);
    }

    @Test
    public void createPayer_shouldThrow_ifFailingToCreateCustomerInGoCardless() {
        when(mockedGoCardlessClientWrapper.createCustomer(PAYMENT_REQUEST_EXTERNAL_ID, payer))
                .thenThrow(new RuntimeException("ooops"));
        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(format("Failed to create customer in gocardless, payment request id: %s, payer id: %s", PAYMENT_REQUEST_EXTERNAL_ID, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerFailedException expected");
        service.createPayer(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount, CREATE_PAYER_REQUEST);
    }

    @Test
    public void createPayer_shouldThrow_ifFailingToCreateCustomerBankAccountInGoCardless() {
        when(mockedGoCardlessClientWrapper.createCustomer(PAYMENT_REQUEST_EXTERNAL_ID, payer))
                .thenReturn(goCardlessCustomer);
        when(mockedGoCardlessClientWrapper.createCustomerBankAccount(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessCustomer, payer.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenThrow(new RuntimeException("oops"));
        thrown.expect(CreateCustomerBankAccountFailedException.class);
        thrown.expectMessage(format("Failed to create customer bank account in gocardless, payment request id: %s, payer id: %s", PAYMENT_REQUEST_EXTERNAL_ID, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
        service.createPayer(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount, CREATE_PAYER_REQUEST);
    }

    @Test
    public void confirm_shouldStoreAGoCardlessMandateAndPaymentWhenReceivingConfirmPaymentRequest() {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), PAYMENT_REQUEST_EXTERNAL_ID)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(PAYMENT_REQUEST_EXTERNAL_ID, mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientWrapper.createPayment(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessMandate, transaction)).thenReturn(goCardlessPayment);
        service.confirm(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount);
        verify(mockedGoCardlessMandateDao).insert(goCardlessMandate);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);
        verify(mockedGoCardlessClientWrapper).createMandate(PAYMENT_REQUEST_EXTERNAL_ID, mandate, goCardlessCustomer);
        verify(mockedGoCardlessClientWrapper).createPayment(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessMandate, transaction);
        verify(mockedTransactionService).paymentCreatedFor(transaction);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToFindCustomerInGoCardless() {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.empty());
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), PAYMENT_REQUEST_EXTERNAL_ID)).thenReturn(confirmationDetails);
        thrown.expectMessage(format("Customer not found in gocardless, payment request id: %s, mandate id: %s", PAYMENT_REQUEST_EXTERNAL_ID, mandate.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CustomerNotFoundException expected");
        service.confirm(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateMandateInGoCardless() {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), PAYMENT_REQUEST_EXTERNAL_ID)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(PAYMENT_REQUEST_EXTERNAL_ID, mandate, goCardlessCustomer)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreateMandateFailedException.class);
        thrown.expectMessage(format("Failed to create mandate in gocardless, payment request id: %s, mandate id: %s", PAYMENT_REQUEST_EXTERNAL_ID, mandate.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateMandateFailedException expected");
        service.confirm(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreatePaymentInGoCardless() {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), PAYMENT_REQUEST_EXTERNAL_ID)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(PAYMENT_REQUEST_EXTERNAL_ID, mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientWrapper.createPayment(PAYMENT_REQUEST_EXTERNAL_ID, goCardlessMandate, transaction)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreatePaymentFailedException.class);
        thrown.expectMessage(format("Failed to create payment in gocardless, payment request id: %s", PAYMENT_REQUEST_EXTERNAL_ID));
        thrown.reportMissingExceptionWithMessage("CreatePaymentFailedException expected");
        service.confirm(PAYMENT_REQUEST_EXTERNAL_ID, gatewayAccount);
    }
}
