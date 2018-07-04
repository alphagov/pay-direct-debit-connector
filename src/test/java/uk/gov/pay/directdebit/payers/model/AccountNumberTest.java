package uk.gov.pay.directdebit.payers.model;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class AccountNumberTest {

    @Test
    public void accountNumberWithExactlySixArabicNumeralsAccepted() {
        AccountNumber.of("123456");
    }

    @Test
    public void accountNumberWithExactlySevenArabicNumeralsAccepted() {
        AccountNumber.of("1234567");
    }

    @Test
    public void accountNumberWithExactlyEightArabicNumeralsAccepted() {
        AccountNumber.of("12345678");
    }

    @Test
    public void accountNumberWithLeadingZeroesAccepted() {
        AccountNumber.of("00123456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithFewerThanSixDigitsNotAccepted() {
        AccountNumber.of("12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithMoreThanEightDigitsNotAccepted() {
        AccountNumber.of("123456789");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithNonArabicNumeralsNotAccepted() {
        AccountNumber.of("1234567١");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithDashesNotAccepted() {
        AccountNumber.of("12-34-56-78");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithSpacesNotAccepted() {
        AccountNumber.of("12 34 56 78");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithHexadecimalDigitsNotAccepted() {
        AccountNumber.of("123456FF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void accountNumberWithLettersNotAccepted() {
        AccountNumber.of("eatcake");
    }

    @Test
    public void twoAccountNumbersWithTheSameDigitsAreEqual() {
        AccountNumber accountNumber1 = AccountNumber.of("12345678");
        AccountNumber accountNumber2 = AccountNumber.of("12345678");

        assertThat(accountNumber1.equals(accountNumber2), is(true));
    }

    @Test
    public void twoAccountNumbersWithDifferentDigitsAreNotEqual() {
        AccountNumber accountNumber1 = AccountNumber.of("12345678");
        AccountNumber accountNumber2 = AccountNumber.of("87654321");

        assertThat(accountNumber1.equals(accountNumber2), is(false));
        assertThat(accountNumber2.equals(accountNumber1), is(false));
    }

    @Test
    public void twoAccountNumbersThatHaveTheSameNumericalValueButAreNotTheSameAreNotEqual() {
        AccountNumber accountNumber1 = AccountNumber.of("1234567");
        AccountNumber accountNumber2 = AccountNumber.of("01234567");

        assertThat(accountNumber1.equals(accountNumber2), is(false));
        assertThat(accountNumber2.equals(accountNumber1), is(false));
    }

    @Test
    public void accountNumberIsNotEqualToTheStringificationOfItself() {
        AccountNumber accountNumber = AccountNumber.of("12345678");

        assertThat(accountNumber.equals("12345678"), is(false));
    }

    @Test
    public void accountNumberIsNotEqualToNull() {
        AccountNumber accountNumber = AccountNumber.of("12345678");

        assertThat(accountNumber.equals(null), is(false));
    }

    @Test
    public void twoAccountNumbersWithTheSameDigitsHaveTheSameHashCode() {
        AccountNumber accountNumber1 = AccountNumber.of("12345678");
        AccountNumber accountNumber2 = AccountNumber.of("12345678");

        assertThat(accountNumber1.hashCode(), is(accountNumber2.hashCode()));
    }

    @Test
    public void twoAccountNumbersWithDifferentDigitsHaveDifferentHashCodes() {
        AccountNumber accountNumber1 = AccountNumber.of("12345678");
        AccountNumber accountNumber2 = AccountNumber.of("87654321");

        assertThat(accountNumber1.hashCode(), not(accountNumber2.hashCode()));
    }

    @Test
    public void accountNumberToStringsToTheStringificationOfItself() {
        AccountNumber accountNumber = AccountNumber.of("12345678");

        assertThat(accountNumber.toString(), is("12345678"));
    }
    
    @Test
    public void getLastTwoDigitsReturnsLastTwoDigitsOfSixDigitAccountNumber() {
        AccountNumber accountNumber = AccountNumber.of("123456");
        
        assertThat(accountNumber.getLastTwoDigits(), is("56"));
    }

    @Test
    public void getLastTwoDigitsReturnsLastTwoDigitsOfSevenDigitAccountNumber() {
        AccountNumber accountNumber = AccountNumber.of("1234567");

        assertThat(accountNumber.getLastTwoDigits(), is("67"));
    }

    @Test
    public void getLastTwoDigitsReturnsLastTwoDigitsOfEightDigitAccountNumber() {
        AccountNumber accountNumber = AccountNumber.of("12345678");

        assertThat(accountNumber.getLastTwoDigits(), is("78"));
    }

    @Test
    public void getLastTwoDigitsReturnsLastTwoDigitsEvenIfThePenultimateOneIsZero() {
        AccountNumber accountNumber = AccountNumber.of("12345609");

        assertThat(accountNumber.getLastTwoDigits(), is("09"));
    }

}
