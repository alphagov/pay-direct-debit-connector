package uk.gov.pay.directdebit.payers.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A UK bank account sort code with no spaces or dashes e.g "123456"
 * Guaranteed to be well-formed (six Arabic numerals) but not necessarily
 * valid (in the sense of representing a real bank and branch)
 */
public class SortCode {

    private static final Pattern SIX_ARABIC_NUMERALS = Pattern.compile("[0-9]{6}");

    private final String sortCode;

    private SortCode(String sortCode) throws IllegalArgumentException {
        Objects.requireNonNull(sortCode);
        if (!SIX_ARABIC_NUMERALS.matcher(sortCode).matches()) {
            throw new IllegalArgumentException("Sort code must consist of exactly six Arabic numerals e.g. \"123456\"");
        }
        this.sortCode = sortCode;
    }

    public static SortCode of(String sortCode) throws IllegalArgumentException {
        return new SortCode(sortCode);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == SortCode.class) {
            SortCode that = (SortCode) other;
            return this.sortCode.equals(that.sortCode);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return sortCode.hashCode();
    }

    @Override
    public String toString() {
        return sortCode;
    }
    
}
