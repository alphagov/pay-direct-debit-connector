package uk.gov.pay.directdebit.common.clients;

import com.gocardless.resources.Creditor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.MandateBankStatementReference;
import uk.gov.pay.directdebit.mandate.model.PaymentProviderMandateIdAndBankReference;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.SortCode;
import uk.gov.pay.directdebit.payments.fixtures.PaymentFixture;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentProviderPaymentIdAndChargeDate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.AUTOGIRO;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.BACS;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.BECS;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.SEPA_CORE;
import static java.time.Month.JULY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessClientFacadeTest {

    private static final String BANK_NAME = "Awesome Bank";
    private static final MandateBankStatementReference BANK_STATEMENT_REFERENCE = MandateBankStatementReference.valueOf("BANK STATEMENT REF");
    private static final GoCardlessMandateId GO_CARDLESS_MANDATE_ID = GoCardlessMandateId.valueOf("MD123");
    private static final GoCardlessPaymentId GO_CARDLESS_PAYMENT_ID = GoCardlessPaymentId.valueOf("PM123");
    private static final BankAccountDetails BANK_ACCOUNT_DETAILS = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));
    private static final LocalDate CHARGE_DATE = LocalDate.of(1969, JULY, 16);

    @Mock
    private GoCardlessClientWrapper mockGoCardlessClientWrapper;

    @Mock
    private com.gocardless.resources.BankDetailsLookup mockBankDetailsLookup;

    @Mock
    private com.gocardless.resources.Mandate mockMandate;

    @Mock
    private com.gocardless.resources.Payment mockPayment;

    @Mock
    private Creditor mockCreditor;

    @Mock
    private Creditor.SchemeIdentifier mockSchemeIdentifier;

    private GoCardlessClientFacade goCardlessClientFacade;

    @Before
    public void setUp() {
        given(mockGoCardlessClientWrapper.validate(BANK_ACCOUNT_DETAILS)).willReturn(mockBankDetailsLookup);

        goCardlessClientFacade = new GoCardlessClientFacade(mockGoCardlessClientWrapper);
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndIsBacsTrueIfBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(BANK_ACCOUNT_DETAILS);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndBacsFalseIfBankNamePresentAndBacsNotInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BECS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(BANK_ACCOUNT_DETAILS);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(false));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndBacsTrueIfBankNamePresentAndBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(BANK_ACCOUNT_DETAILS);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithNullBankNameAndBacsTrueIfBankNamePresentAndBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(null);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(BANK_ACCOUNT_DETAILS);

        assertThat(result.getBankName(), is(nullValue()));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithNullBankNameAndBacsFalseIfBankNamePresentAndBacsNotInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(null);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Collections.emptyList());

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(BANK_ACCOUNT_DETAILS);

        assertThat(result.getBankName(), is(nullValue()));
        assertThat(result.isBacs(), is(false));
    }

    @Test
    public void getSunName_shouldReturnSunNameWhenBacsIsPresent() {
        SunName sunName = SunName.of("testServiceUserNumber");
        given(mockGoCardlessClientWrapper.getCreditor()).willReturn(mockCreditor);
        given(mockCreditor.getSchemeIdentifiers()).willReturn(Collections.singletonList(mockSchemeIdentifier));
        given(mockSchemeIdentifier.getScheme()).willReturn(Creditor.SchemeIdentifier.Scheme.BACS);
        given(mockSchemeIdentifier.getName()).willReturn(sunName.toString());

        Optional<SunName> result = goCardlessClientFacade.getSunName();

        assertThat(result, is(Optional.of(sunName)));
    }

    @Test
    public void getSunName_shouldReturnEmptyWhenCreditorIdHasNoSunName() {
        given(mockGoCardlessClientWrapper.getCreditor()).willReturn(mockCreditor);
        given(mockCreditor.getSchemeIdentifiers()).willReturn(Collections.singletonList(mockSchemeIdentifier));
        given(mockSchemeIdentifier.getScheme()).willReturn(Creditor.SchemeIdentifier.Scheme.SEPA);

        Optional<SunName> result = goCardlessClientFacade.getSunName();

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void createMandateReturnsGoCardlessMandateIdAndBankStatementReference() {
        var mandate = MandateFixture.aMandateFixture().toEntity();
        GoCardlessCustomer goCardlessCustomer = GoCardlessCustomerFixture.aGoCardlessCustomerFixture().toEntity();

        given(mockMandate.getId()).willReturn(GO_CARDLESS_MANDATE_ID.toString());
        given(mockMandate.getReference()).willReturn(BANK_STATEMENT_REFERENCE.toString());

        given(mockGoCardlessClientWrapper.createMandate(mandate.getExternalId(), goCardlessCustomer)).willReturn(mockMandate);

        PaymentProviderMandateIdAndBankReference result = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);

        assertThat(result.getPaymentProviderMandateId(), is(GO_CARDLESS_MANDATE_ID));
        assertThat(result.getMandateBankStatementReference(), is(BANK_STATEMENT_REFERENCE));
    }

    @Test
    public void createPaymentReturnsGoCardlessPaymentIdAndChargeDate() {
        var payment = PaymentFixture.aPaymentFixture().toEntity();

        given(mockPayment.getId()).willReturn(GO_CARDLESS_PAYMENT_ID.toString());
        given(mockPayment.getChargeDate()).willReturn(CHARGE_DATE.toString());

        given(mockGoCardlessClientWrapper.createPayment(payment, GO_CARDLESS_MANDATE_ID)).willReturn(mockPayment);

        PaymentProviderPaymentIdAndChargeDate result = goCardlessClientFacade.createPayment(payment, GO_CARDLESS_MANDATE_ID);

        assertThat(result.getPaymentProviderPaymentId(), is(GO_CARDLESS_PAYMENT_ID));
        assertThat(result.getChargeDate(), is(CHARGE_DATE));
    }

}
