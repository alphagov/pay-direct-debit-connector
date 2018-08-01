package uk.gov.pay.directdebit.payments.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFactory;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorId;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessServiceUserName;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessMandateDao;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.fixtures.GoCardlessMandateFixture;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateType;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.mandate.services.gocardless.GoCardlessService;
import uk.gov.pay.directdebit.payers.api.BankAccountValidationResponse;
import uk.gov.pay.directdebit.payers.dao.GoCardlessCustomerDao;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.fixtures.PayerFixture;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerBankAccountFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateCustomerFailedException;
import uk.gov.pay.directdebit.payments.exception.CreateMandateFailedException;
import uk.gov.pay.directdebit.payments.exception.CreatePaymentFailedException;
import uk.gov.pay.directdebit.payments.fixtures.GatewayAccountFixture;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Optional;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.pay.directdebit.mandate.fixtures.GoCardlessPaymentFixture.aGoCardlessPaymentFixture;
import static uk.gov.pay.directdebit.mandate.fixtures.MandateFixture.aMandateFixture;

public abstract class GoCardlessServiceTest {

    static final String CUSTOMER_ID = "CU328471";
    static final String BANK_ACCOUNT_ID = "BA34983496";
    static final MandateExternalId MANDATE_ID = MandateExternalId.of("sdkfhsdkjfhjdks");
    static final String TRANSACTION_ID = "sdkfhsd2jfhjdks";
    static final SortCode SORT_CODE = SortCode.of("123456");
    static final AccountNumber ACCOUNT_NUMBER = AccountNumber.of("12345678");

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    protected GoCardlessClientFacade mockedGoCardlessClientFacade;
    @Mock
    protected GoCardlessMandateDao mockedGoCardlessMandateDao;
    @Mock
    protected GoCardlessCustomerDao mockedGoCardlessCustomerDao;
    @Mock
    protected GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    @Mock
    protected GoCardlessClientFactory mockedGoCardlessClientFactory;

    GoCardlessService service;

    GatewayAccountFixture gatewayAccountFixture = GatewayAccountFixture
            .aGatewayAccountFixture().withPaymentProvider(PaymentProvider.GOCARDLESS);
    PayerFixture payerFixture = PayerFixture.aPayerFixture();
    GoCardlessCustomer goCardlessCustomer = GoCardlessCustomerFixture.aGoCardlessCustomerFixture()
            .withPayerId(payerFixture.getId())
            .withCustomerId(CUSTOMER_ID)
            .withCustomerBankAccountId(BANK_ACCOUNT_ID).toEntity();
    MandateFixture mandateFixture = aMandateFixture()
            .withPayerFixture(payerFixture)
            .withMandateType(MandateType.ON_DEMAND)
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withExternalId(MANDATE_ID);
    Transaction transaction = TransactionFixture.aTransactionFixture()
            .withMandateFixture(mandateFixture)
            .withExternalId(TRANSACTION_ID)
            .toEntity();
    GoCardlessCreditorId goCardlessCreditorId = GoCardlessCreditorId.of("test_creditor_id");
    GoCardlessMandate goCardlessMandate =
            GoCardlessMandateFixture.aGoCardlessMandateFixture()
                    .withGoCardlessCreditorId(goCardlessCreditorId)
                    .withMandateId(mandateFixture.getId())
                    .toEntity();
    GoCardlessPayment goCardlessPayment = aGoCardlessPaymentFixture().withTransactionId(transaction.getId()).toEntity();
    BankAccountDetails bankAccountDetails = new BankAccountDetails(ACCOUNT_NUMBER, SORT_CODE);

    @Test
    public void shouldValidateBankAccountDetails() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123456");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
        GoCardlessBankAccountLookup goCardlessBankAccountLookup = new GoCardlessBankAccountLookup("Great Bank of Cake", true);

        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenReturn(goCardlessBankAccountLookup);

        BankAccountValidationResponse response = service.validate(mandateFixture.toEntity(), bankAccountDetails);
        assertThat(response.isValid(), is(true));
        assertThat(response.getBankName(), is("Great Bank of Cake"));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifGoCardlessReturnsInvalidLookup() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);
        GoCardlessBankAccountLookup goCardlessBankAccountLookup = new GoCardlessBankAccountLookup(null, false);

        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenReturn(goCardlessBankAccountLookup);

        BankAccountValidationResponse response = service.validate(mandateFixture.toEntity(), bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldValidateBankAccountDetails_ifExceptionIsThrownFromGC() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);

        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenThrow(new RuntimeException());

        BankAccountValidationResponse response = service.validate(mandateFixture.toEntity(), bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldReturnNotEmptyServiceUserNameOptional_whenBacsSchemeExists() {
        GoCardlessServiceUserName goCardlessServiceUserName = GoCardlessServiceUserName.of("testServiceUserName");
        Mandate mandate = mandateFixture.toEntity();

        given(mockedGoCardlessMandateDao.findByMandateId(mandateFixture.getId())).willReturn(Optional.of(goCardlessMandate));
        given(mockedGoCardlessClientFacade.getServiceUserName(goCardlessCreditorId)).willReturn(Optional.of(goCardlessServiceUserName));

        assertThat(service.getServiceUserName(mandate), is(Optional.of(goCardlessServiceUserName)));
    }

    @Test
    public void shouldReturnEmptyServiceUserNameOptional_whenBacsSchemeDoesNotExist() {
        given(mockedGoCardlessMandateDao.findByMandateId(mandateFixture.getId())).willReturn(Optional.of(goCardlessMandate));
        given(mockedGoCardlessClientFacade.getServiceUserName(goCardlessCreditorId)).willReturn(Optional.empty());

        assertThat(service.getServiceUserName(mandateFixture.toEntity()), is(Optional.empty()));
    }

    void verifyMandateFailedException() {
        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenThrow(new RuntimeException("gocardless said no"));

        thrown.expect(CreateMandateFailedException.class);
        thrown.expectMessage(format("Failed to create mandate in gocardless, mandate id: %s", MANDATE_ID));
        thrown.reportMissingExceptionWithMessage("CreateMandateFailedException expected");
    }

    void verifyCreatePaymentFailedException() {
        when(mockedGoCardlessClientFacade.createMandate(mandateFixture.toEntity(), goCardlessCustomer)).thenReturn(goCardlessMandate);
        when(mockedGoCardlessClientFacade.createPayment(transaction, goCardlessMandate)).thenThrow(new RuntimeException("gocardless said no"));

        thrown.expect(CreatePaymentFailedException.class);
        thrown.expectMessage(format("Failed to create payment in gocardless, mandate id: %s, transaction id: %s",
                MANDATE_ID, TRANSACTION_ID));
        thrown.reportMissingExceptionWithMessage("CreatePaymentFailedException expected");
    }

    void verifyCreateCustomerBankAccountFailedException() {
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenThrow(new RuntimeException("oops"));

        thrown.expect(CreateCustomerBankAccountFailedException.class);
        thrown.expectMessage(format("Failed to create customer bank account in gocardless, mandate id: %s, payer id: %s",
                MANDATE_ID, payerFixture.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
    }

    void verifyCreateCustomerFailedException() {
        when(mockedGoCardlessClientFacade.createCustomer(MANDATE_ID, payerFixture.toEntity()))
                .thenThrow(new RuntimeException("ooops"));

        thrown.expect(CreateCustomerFailedException.class);
        thrown.expectMessage(format("Failed to create customer in gocardless, mandate id: %s, payer id: %s",
                MANDATE_ID, payerFixture.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerFailedException expected");
    }
}
