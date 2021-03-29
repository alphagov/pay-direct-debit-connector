package uk.gov.pay.directdebit.payers.model;

import uk.gov.service.payments.commons.model.WrappedStringValue;

import java.util.regex.Pattern;

/**
 * A UK bank account number with no dashes or spaces e.g "12345678"
 * Guaranteed to be well-formed (six to eight Arabic numerals
 * inclusive) but not necessarily valid (in the sense of representing
 * a real bank account)
 */
public class AccountNumber extends WrappedStringValue {

    private static final Pattern SIX_TO_EIGHT_ARABIC_NUMERALS = Pattern.compile("[0-9]{6,8}");

    private AccountNumber(String accountNumber) throws IllegalArgumentException {
        super(accountNumber);
        if (!SIX_TO_EIGHT_ARABIC_NUMERALS.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException("Account number must consist of six to eight Arabic numerals inclusive e.g. \"12345678\"");
        }
    }

    public static AccountNumber of(String accountNumber) throws IllegalArgumentException {
        return new AccountNumber(accountNumber);
    }

    public String getLastTwoDigits() {
        String accountNumber = toString();
        return accountNumber.substring(accountNumber.length() - 2);
    }

}
