package uk.gov.pay.directdebit.payments.clients;

import com.gocardless.resources.BankDetailsLookup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;

import java.util.Arrays;
import java.util.Collections;

import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.AUTOGIRO;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.BACS;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.BECS;
import static com.gocardless.resources.BankDetailsLookup.AvailableDebitScheme.SEPA_CORE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class GoCardlessClientFacadeTest {

    private static final String BANK_NAME = "Awesome Bank";

    private final BankAccountDetails bankAccountDetails = new BankAccountDetails("12345678", "123456");

    @Mock
    private GoCardlessClientWrapper mockGoCardlessClientWrapper;

    @Mock
    private BankDetailsLookup mockBankDetailsLookup;

    private GoCardlessClientFacade goCardlessClientFacade;

    @Before
    public void setUp() {
        given(mockGoCardlessClientWrapper.validate(bankAccountDetails)).willReturn(mockBankDetailsLookup);

        goCardlessClientFacade = new GoCardlessClientFacade(mockGoCardlessClientWrapper);
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndIsBacsTrueIfBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(bankAccountDetails);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndBacsFalseIfBankNamePresentAndBacsNotInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BECS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(bankAccountDetails);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(false));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithBankNameAndBacsTrueIfBankNamePresentAndBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(BANK_NAME);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(bankAccountDetails);

        assertThat(result.getBankName(), is(BANK_NAME));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithNullBankNameAndBacsTrueIfBankNamePresentAndBacsInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(null);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Arrays.asList(AUTOGIRO, BACS, SEPA_CORE));

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(bankAccountDetails);

        assertThat(result.getBankName(), is(nullValue()));
        assertThat(result.isBacs(), is(true));
    }

    @Test
    public void validateReturnsGoCardlessBankAccountLookupWithNullBankNameAndBacsFalseIfBankNamePresentAndBacsNotInAvailableSchemes() {
        given(mockBankDetailsLookup.getBankName()).willReturn(null);
        given(mockBankDetailsLookup.getAvailableDebitSchemes()).willReturn(Collections.emptyList());

        GoCardlessBankAccountLookup result = goCardlessClientFacade.validate(bankAccountDetails);

        assertThat(result.getBankName(), is(nullValue()));
        assertThat(result.isBacs(), is(false));
    }

}
