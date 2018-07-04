package uk.gov.pay.directdebit.payments.services;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.ConfirmationDetails;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.SortCode;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;
import static uk.gov.pay.directdebit.payments.fixtures.GoCardlessEventFixture.aGoCardlessEventFixture;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessServiceOneOffTest extends GoCardlessServiceTest {

    @Before
    public void setUp() {
        confirmationDetails = new ConfirmationDetails(mandateFixture.toEntity(), transaction, ACCOUNT_NUMBER, SORT_CODE);
        service = new GoCardlessService(mockedPayerService, mockedTransactionService,
                mockedMandateService, mockedGoCardlessClientFacade,
                mockedGoCardlessCustomerDao, mockedGoCardlessPaymentDao, mockedGoCardlessMandateDao, mockedGoCardlessEventDao,
                mockedMandateDao, mockedBankAccountDetailsParser);
        goCardlessCustomer = new GoCardlessCustomer(null, payerFixture.getId(), CUSTOMER_ID, BANK_ACCOUNT_ID);
        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity())).thenReturn(goCardlessCustomer);
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER)).thenReturn(goCardlessCustomer);
        when(mockedMandateService.confirm(MANDATE_ID, confirmDetails)).thenReturn(confirmationDetails);
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
        service.findGoCardlessMandateForEvent(goCardlessEvent);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerInGoCardless() {
        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity()))
                .thenThrow(new RuntimeException("ooops"));
        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(format("Failed to create customer in gocardless, mandate id: %s, payer id: %s",
                MANDATE_ID, payerFixture.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerFailedException expected");
        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateCustomerBankAccountInGoCardless() {
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenThrow(new RuntimeException("oops"));
        thrown.expect(CreateCustomerBankAccountFailedException.class);
        thrown.expectMessage(format("Failed to create customer bank account in gocardless, mandate id: %s, payer id: %s",
                MANDATE_ID, payerFixture.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
    }

    @Test
    public void confirm_shouldStoreAGoCardlessCustomerBankAccountMandateAndPaymentWhenReceivingConfirmTransaction() {
        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate)).thenReturn(goCardlessPayment);

        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
        verify(mockedGoCardlessCustomerDao).insert(goCardlessCustomer);
        verify(mockedGoCardlessMandateDao).insert(goCardlessMandate);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);
        verify(mockedMandateDao).updateMandateReference(mandateFixture.getId(), goCardlessMandate.getGoCardlessReference());
        InOrder orderedCalls = inOrder(mockedGoCardlessClientFacade);

        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomer(MANDATE_ID, payerFixture.toEntity());
        orderedCalls.verify(mockedGoCardlessClientFacade).createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER);
        orderedCalls.verify(mockedGoCardlessClientFacade).createMandate(mandateFixture.toEntity(), goCardlessCustomer);
        orderedCalls.verify(mockedGoCardlessClientFacade).createPayment(transaction, goCardlessMandate);

        verify(mockedTransactionService).oneOffPaymentSubmittedToProviderFor(transaction, goCardlessPayment.getChargeDate());
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreateMandateInGoCardless() {
        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreateMandateFailedException.class);
        thrown.expectMessage(format("Failed to create mandate in gocardless, mandate id: %s", MANDATE_ID));
        thrown.reportMissingExceptionWithMessage("CreateMandateFailedException expected");
        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
    }

    @Test
    public void confirm_shouldThrow_ifFailingToCreatePaymentInGoCardless() {
        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate)).thenThrow(new RuntimeException("gocardless said no"));
        thrown.expect(CreatePaymentFailedException.class);
        thrown.expectMessage(format("Failed to create payment in gocardless, mandate id: %s, transaction id: %s",
                MANDATE_ID, TRANSACTION_ID));
        thrown.reportMissingExceptionWithMessage("CreatePaymentFailedException expected");
        service.confirm(MANDATE_ID, gatewayAccountFixture.toEntity(), confirmDetails);
    }

    @Test
    public void shouldValidateBankAccountDetails() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123456");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
        Map<String, String> bankAccountDetailsRequest = ImmutableMap.of(
                "account_number", accountNumber.toString(),
                "sort_code", sortCode.toString()
        );
        GoCardlessBankAccountLookup goCardlessBankAccountLookup = new GoCardlessBankAccountLookup("Great Bank of Cake", true);
        when(mockedBankAccountDetailsParser.parse(bankAccountDetailsRequest)).thenReturn(bankAccountDetails);
        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenReturn(goCardlessBankAccountLookup);
        BankAccountValidationResponse response = service.validate(MANDATE_ID, bankAccountDetailsRequest);
        assertThat(response.isValid(), is(true));
        assertThat(response.getBankName(), is("Great Bank of Cake"));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifGoCardlessReturnsInvalidLookup() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
        Map<String, String> bankAccountDetailsRequest = ImmutableMap.of(
                "account_number", accountNumber.toString(),
                "sort_code", sortCode.toString()
        );
        GoCardlessBankAccountLookup goCardlessBankAccountLookup = new GoCardlessBankAccountLookup(null, false);
        when(mockedBankAccountDetailsParser.parse(bankAccountDetailsRequest)).thenReturn(bankAccountDetails);
        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenReturn(goCardlessBankAccountLookup);
        BankAccountValidationResponse response = service.validate(MANDATE_ID, bankAccountDetailsRequest);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifExceptionIsThrownFromGC() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
        Map<String, String> bankAccountDetailsRequest = ImmutableMap.of(
                "account_number", accountNumber.toString(),
                "sort_code", sortCode.toString()
        );
        when(mockedBankAccountDetailsParser.parse(bankAccountDetailsRequest)).thenReturn(bankAccountDetails);
        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenThrow(new RuntimeException());
        BankAccountValidationResponse response = service.validate(MANDATE_ID, bankAccountDetailsRequest);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void collect_shouldCreateAGoCardlessPaymentAndRegiseterOnDemandPaymentSubmittedEvent() {
        MandateFixture mandateFixture = aMandateFixture().withMandateType(MandateType.ON_DEMAND).withGatewayAccountFixture(gatewayAccountFixture);
        GoCardlessMandate goCardlessMandate = GoCardlessMandateFixture.aGoCardlessMandateFixture().withMandateId(mandateFixture.getId()).toEntity();
        Transaction transaction = TransactionFixture.aTransactionFixture().withMandateFixture(mandateFixture).toEntity();
        Mandate mandate = mandateFixture.toEntity();
        Map<String, String> collectPaymentRequest = ImmutableMap.of(
                "amount", "123456",
                "reference", "a reference",
                "description", "a description",
                "agreement_id", mandateFixture.getExternalId());

        when(mockedMandateService.findByExternalId(mandateFixture.getExternalId())).thenReturn(mandate);
        when(mockedTransactionService
                .createTransaction(collectPaymentRequest, mandate, gatewayAccountFixture.getExternalId()))
                .thenReturn(transaction);
        when(mockedGoCardlessMandateDao
                .findByMandateId(mandateFixture.getId()))
                .thenReturn(Optional.of(goCardlessMandate));
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate)).thenReturn(goCardlessPayment);
        service.collect(gatewayAccountFixture.toEntity(), collectPaymentRequest);
        verify(mockedGoCardlessPaymentDao).insert(goCardlessPayment);
        verify(mockedGoCardlessClientFacade).createPayment(transaction, goCardlessMandate);
        verify(mockedTransactionService).onDemandPaymentSubmittedToProviderFor(transaction, goCardlessPayment.getChargeDate());
    }
}
