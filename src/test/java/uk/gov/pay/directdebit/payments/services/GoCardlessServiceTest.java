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

    @Mock
    PayerService mockedPayerService;
    @Mock
    GoCardlessClientWrapper mockedGoCardlessClientWrapper;
    @Mock
    GoCardlessCustomerDao mockedGoCardlessCustomerDao;
    @Mock
    GoCardlessMandateDao mockedGoCardlessMandateDao;
    @Mock
    GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    @Mock
    PaymentConfirmService mockedPaymentConfirmService;
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
    private Mandate mandate = MandateFixture.aMandateFixture().withPayerId(payer.getId()).toEntity();
    private Transaction transaction = TransactionFixture.aTransactionFixture().toEntity();
    private GoCardlessMandate goCardlessMandate =
            aGoCardlessMandateFixture()
            .withMandateId(mandate.getId()).toEntity();
    private GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().toEntity();
    private GatewayAccount gatewayAccount = aGatewayAccountFixture().toEntity();

    private ConfirmationDetails confirmationDetails = new ConfirmationDetails(transaction, mandate);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        service = new GoCardlessService(mockedPayerService, mockedPaymentConfirmService, mockedGoCardlessClientWrapper, mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao);
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
    public void shouldThrowIfNoPaymentIsFoundForEvent() {
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
    public void shouldThrowIfNoMandateIsFoundForEvent() {
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
    public void shouldThrow_ifFailingToCreateCustomerInGoCardless()  {
        when(mockedGoCardlessClientWrapper.createCustomer(paymentRequestExternalId, payer))
                .thenThrow(new RuntimeException("ooops"));
        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(format("Failed to create customer in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payer.getExternalId()));
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
        thrown.expectMessage(format("Failed to create customer bank account in gocardless, payment request id: %s, payer id: %s", paymentRequestExternalId, payer.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
        service.createPayer(paymentRequestExternalId, gatewayAccount, createPayerRequest);
    }

    @Test
    public void shouldStoreAGoCardlessMandateAndPaymentWhenReceivingConfirmPaymentRequest() {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(paymentRequestExternalId, mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientWrapper.createPayment(paymentRequestExternalId, goCardlessMandate, transaction)).thenReturn(goCardlessPayment);
        service.confirm(paymentRequestExternalId, gatewayAccount);
        verify(mockedGoCardlessMandateDao).insert(goCardlessMandate);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);
        verify(mockedGoCardlessClientWrapper).createMandate(paymentRequestExternalId, mandate, goCardlessCustomer);
        verify(mockedGoCardlessClientWrapper).createPayment(paymentRequestExternalId, goCardlessMandate, transaction);
    }

    @Test
    public void shouldThrow_ifFailingToFindCustomerInGoCardless()  {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.empty());
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId)).thenReturn(confirmationDetails);
        thrown.expectMessage(format("Customer not found in gocardless, payment request id: %s, mandate id: %s", paymentRequestExternalId, mandate.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CustomerNotFoundException expected");
        service.confirm(paymentRequestExternalId, gatewayAccount);
    }

    @Test
    public void shouldThrow_ifFailingToCreateMandateInGoCardless()  {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(paymentRequestExternalId, mandate, goCardlessCustomer)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreateMandateFailedException.class);
        thrown.expectMessage(format("Failed to create mandate in gocardless, payment request id: %s, mandate id: %s", paymentRequestExternalId, mandate.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateMandateFailedException expected");
        service.confirm(paymentRequestExternalId, gatewayAccount);
    }

    @Test
    public void shouldThrow_ifFailingToCreatePaymentInGoCardless()  {
        when(mockedGoCardlessCustomerDao.findByPayerId(payer.getId())).thenReturn(Optional.of(goCardlessCustomer));
        when(mockedPaymentConfirmService.confirm(gatewayAccount.getId(), paymentRequestExternalId)).thenReturn(confirmationDetails);
        when(mockedGoCardlessClientWrapper.createMandate(paymentRequestExternalId, mandate, goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientWrapper.createPayment(paymentRequestExternalId, goCardlessMandate, transaction)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreatePaymentFailedException.class);
        thrown.expectMessage(format("Failed to create payment in gocardless, payment request id: %s", paymentRequestExternalId));
        thrown.reportMissingExceptionWithMessage("CreatePaymentFailedException expected");
        service.confirm(paymentRequestExternalId, gatewayAccount);
    }
}
