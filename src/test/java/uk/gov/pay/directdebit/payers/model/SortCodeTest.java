package uk.gov.pay.directdebit.payers.model;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class SortCodeTest {

    @Test
    public void sortCodeWithExactlySixArabicNumeralsAccepted() {
        SortCode.of("123456");
    }

    @Test
    public void sortCodeWithLeadingZeroesAccepted() {
        SortCode.of("073436");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithFewerThanSixDigitsNotAccepted() {
        SortCode.of("12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithMoreThanSixDigitsNotAccepted() {
        SortCode.of("1234567");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithNonArabicNumeralsNotAccepted() {
        SortCode.of("12345١");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithDashesNotAccepted() {
        SortCode.of("12-34-56");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithSpacesNotAccepted() {
        SortCode.of("12 34 56");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithHexadecimalDigitsNotAccepted() {
        SortCode.of("1234FF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortCodeWithLettersNotAccepted() {
        SortCode.of("sextus");
    }
    
    @Test
    public void twoSortCodesWithTheSameDigitsAreEqual() {
        SortCode sortCode1 = SortCode.of("123456");
        SortCode sortCode2 = SortCode.of("123456");
        
        assertThat(sortCode1.equals(sortCode2), is(true));
    }

    @Test
    public void twoSortCodesWithDifferentDigitsAreNotEqual() {
        SortCode sortCode1 = SortCode.of("123456");
        SortCode sortCode2 = SortCode.of("654321");

        assertThat(sortCode1.equals(sortCode2), is(false));
        assertThat(sortCode2.equals(sortCode1), is(false));
    }

    @Test
    public void sortCodeIsNotEqualToTheStringificationOfItself() {
        SortCode sortCode = SortCode.of("123456");

        assertThat(sortCode.equals("123456"), is(false));
    }

    @Test
    public void sortCodeIsNotEqualToNull() {
        SortCode sortCode = SortCode.of("123456");

        assertThat(sortCode.equals(null), is(false));
    }
    
    @Test
    public void twoSortCodesWithTheSameDigitsHaveTheSameHashCode() {
        SortCode sortCode1 = SortCode.of("123456");
        SortCode sortCode2 = SortCode.of("123456");

        assertThat(sortCode1.hashCode(), is(sortCode2.hashCode()));
    }

    @Test
    public void twoSortCodesWithDifferentDigitsHaveDifferentHashCodes() {
        SortCode sortCode1 = SortCode.of("123456");
        SortCode sortCode2 = SortCode.of("654321");

        assertThat(sortCode1.hashCode(), not(sortCode2.hashCode()));
    }

    @Test
    public void sortCodeToStringsToTheStringificationOfItself() {
        SortCode sortCode = SortCode.of("123456");
        
        assertThat(sortCode.toString(), is("123456"));
    }

}
