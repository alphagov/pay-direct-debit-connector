package uk.gov.pay.directdebit.payments.services;

import com.gocardless.errors.GoCardlessApiException;
import com.gocardless.errors.ValidationFailedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFacade;
import uk.gov.pay.directdebit.common.clients.GoCardlessClientFactory;
import uk.gov.pay.directdebit.common.exception.InternalServerErrorException;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.dao.GoCardlessPaymentDao;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;
import uk.gov.pay.directdebit.mandate.model.Mandate;
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

    private static final String CUSTOMER_ID = "CU328471";
    static final String BANK_ACCOUNT_ID = "BA34983496";
    static final MandateExternalId MANDATE_ID = MandateExternalId.valueOf("sdkfhsdkjfhjdks");
    private static final String TRANSACTION_ID = "sdkfhsd2jfhjdks";
    static final SortCode SORT_CODE = SortCode.of("123456");
    static final AccountNumber ACCOUNT_NUMBER = AccountNumber.of("12345678");

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    protected GoCardlessClientFacade mockedGoCardlessClientFacade;
    @Mock
    protected GoCardlessCustomerDao mockedGoCardlessCustomerDao;
    @Mock
    protected GoCardlessPaymentDao mockedGoCardlessPaymentDao;
    @Mock
    protected GoCardlessClientFactory mockedGoCardlessClientFactory;
    @Mock
    protected GoCardlessApiException mockedGoCardlessException;
    @Mock
    protected ValidationFailedException mockedGoCardlessValidationFailedException;

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
            .withGatewayAccountFixture(gatewayAccountFixture)
            .withExternalId(MANDATE_ID);
    Transaction transaction = TransactionFixture.aTransactionFixture()
            .withMandateFixture(mandateFixture)
            .withExternalId(TRANSACTION_ID)
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
    public void shouldValidateBankAccountDetails_ifValidationFailedExceptionIsThrownFromGC() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);

        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenThrow(mockedGoCardlessValidationFailedException);

        BankAccountValidationResponse response = service.validate(mandateFixture.toEntity(), bankAccountDetails);
        assertThat(response.isValid(), is(false));
        assertThat(response.getBankName(), is(nullValue()));
    }

    @Test
    public void shouldThrowInternalServerException_ifExceptionIsThrownFromGC() {
        AccountNumber accountNumber = AccountNumber.of("12345678");
        SortCode sortCode = SortCode.of("123467");
        BankAccountDetails bankAccountDetails = new BankAccountDetails(accountNumber, sortCode);

        when(mockedGoCardlessClientFacade.validate(bankAccountDetails)).thenThrow(mockedGoCardlessException);

        thrown.expect(InternalServerErrorException.class);
        service.validate(mandateFixture.toEntity(), bankAccountDetails);
    }

    @Test
    public void shouldReturnSunNameForCreditorId() {
        SunName sunName = SunName.of("testServiceUserNumber");
        Mandate mandate = mandateFixture.toEntity();

        given(mockedGoCardlessClientFacade.getSunName()).willReturn(Optional.of(sunName));

        assertThat(service.getSunName(mandate), is(Optional.of(sunName)));
    }

    @Test
    public void shouldReturnEmptyWhenCreditorIdHasNoSunName() {
        assertThat(service.getSunName(mandateFixture.toEntity()), is(Optional.empty()));
    }

    void verifyCreateCustomerBankAccountFailedException() {
        when(mockedGoCardlessClientFacade.createCustomerBankAccount(MANDATE_ID, goCardlessCustomer, payerFixture.getName(), SORT_CODE, ACCOUNT_NUMBER))
                .thenThrow(new RuntimeException("oops"));

        thrown.expect(CreateCustomerBankAccountFailedException.class);
        thrown.expectMessage(format("Failed to create customer bank account in gocardless, mandate id: %s, payer id: %s",
                MANDATE_ID, payerFixture.getExternalId()));
        thrown.reportMissingExceptionWithMessage("CreateCustomerBankAccountFailedException expected");
    }

}
