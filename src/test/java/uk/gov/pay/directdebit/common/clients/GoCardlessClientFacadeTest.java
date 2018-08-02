package uk.gov.pay.directdebit.common.clients;

import com.gocardless.resources.BankDetailsLookup;
import com.gocardless.resources.Creditor;
import com.gocardless.resources.Mandate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.directdebit.common.model.subtype.SunName;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorId;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.payers.fixtures.GoCardlessCustomerFixture;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.BankAccountDetails;
import uk.gov.pay.directdebit.payers.model.GoCardlessBankAccountLookup;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;
import uk.gov.pay.directdebit.payers.model.SortCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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

    private final BankAccountDetails bankAccountDetails = new BankAccountDetails(AccountNumber.of("12345678"), SortCode.of("123456"));

    @Mock
    private GoCardlessClientWrapper mockGoCardlessClientWrapper;

    @Mock
    private BankDetailsLookup mockBankDetailsLookup;

    @Mock
    private Mandate mockMandate;

    @Mock
    private Mandate.Links mockMandateLinks;

    @Mock
    private Creditor mockCreditor;

    @Mock
    private Creditor.SchemeIdentifier mockSchemeIdentifier;

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

    @Test
    public void getSunName_shouldReturnSunNameWhenBacsIsPresent() {
        String creditorId = "creditor-id-123";
        SunName sunName = SunName.of("testServiceUserNumber");
        given(mockGoCardlessClientWrapper.getCreditor(creditorId)).willReturn(mockCreditor);
        given(mockCreditor.getSchemeIdentifiers()).willReturn(Collections.singletonList(mockSchemeIdentifier));
        given(mockSchemeIdentifier.getScheme()).willReturn(Creditor.SchemeIdentifier.Scheme.BACS);
        given(mockSchemeIdentifier.getName()).willReturn(sunName.toString());

        Optional<SunName> result = goCardlessClientFacade.getSunName(GoCardlessCreditorId.of(creditorId));

        assertThat(result, is(Optional.of(sunName)));
    }

    @Test
    public void getSunName_shouldReturnEmptyWhenCreditorIdHasNoSunName() {
        String creditorId = "creditor-id-123";
        given(mockGoCardlessClientWrapper.getCreditor(creditorId)).willReturn(mockCreditor);
        given(mockCreditor.getSchemeIdentifiers()).willReturn(Collections.singletonList(mockSchemeIdentifier));
        given(mockSchemeIdentifier.getScheme()).willReturn(Creditor.SchemeIdentifier.Scheme.SEPA);

        Optional<SunName> result = goCardlessClientFacade.getSunName(GoCardlessCreditorId.of(creditorId));

        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void createMandateReturnsGoCardlessMandate() {
        GoCardlessCreditorId goCardlessCreditorId = GoCardlessCreditorId.of("gocardless-test-creditor-id-here");
        uk.gov.pay.directdebit.mandate.model.Mandate mandate =
                MandateFixture.aMandateFixture().toEntity();
        GoCardlessCustomer goCardlessCustomer =
                GoCardlessCustomerFixture.aGoCardlessCustomerFixture().toEntity();

        String goCardlessReference = "test-gocardless-mandate-reference-here";
        String goCardlessMandateId = "test-gocardless-mandate-id-here";
        given(mockMandate.getId()).willReturn(goCardlessMandateId);
        given(mockMandate.getReference()).willReturn(goCardlessReference);
        given(mockMandate.getLinks()).willReturn(mockMandateLinks);
        given(mockMandateLinks.getCreditor()).willReturn(goCardlessCreditorId.toString());

        given(mockGoCardlessClientWrapper.createMandate(mandate.getExternalId(), goCardlessCustomer)).willReturn(mockMandate);

        GoCardlessMandate result = goCardlessClientFacade.createMandate(mandate, goCardlessCustomer);

        assertThat(result.getGoCardlessMandateId(), is(goCardlessMandateId));
        assertThat(result.getGoCardlessReference(), is(goCardlessReference));
        assertThat(result.getGoCardlessCreditorId(), is(goCardlessCreditorId));
    }
}
