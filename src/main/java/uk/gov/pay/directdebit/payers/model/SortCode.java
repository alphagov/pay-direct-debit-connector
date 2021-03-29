package uk.gov.pay.directdebit.payers.model;

import uk.gov.service.payments.commons.model.WrappedStringValue;

import java.util.regex.Pattern;

/**
 * A UK bank account sort code with no spaces or dashes e.g "123456"
 * Guaranteed to be well-formed (six Arabic numerals) but not necessarily
 * valid (in the sense of representing a real bank and branch)
 */
public class SortCode extends WrappedStringValue {

    private static final Pattern SIX_ARABIC_NUMERALS = Pattern.compile("[0-9]{6}");

    private SortCode(String sortCode) throws IllegalArgumentException {
        super(sortCode);
        if (!SIX_ARABIC_NUMERALS.matcher(sortCode).matches()) {
            throw new IllegalArgumentException("Sort code must consist of exactly six Arabic numerals e.g. \"123456\"");
        }
    }

    public static SortCode of(String sortCode) throws IllegalArgumentException {
        return new SortCode(sortCode);
    }

}
