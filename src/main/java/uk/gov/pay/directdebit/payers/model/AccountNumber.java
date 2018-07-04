package uk.gov.pay.directdebit.payers.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A UK bank account number with no dashes or spaces e.g "12345678"
 * Guaranteed to be well-formed (six to eight Arabic numerals
 * inclusive) but not necessarily valid (in the sense of representing
 * a real bank account)
 */
public class AccountNumber {

    private static final Pattern SIX_TO_EIGHT_ARABIC_NUMERALS = Pattern.compile("[0-9]{6,8}");

    private final String accountNumber;

    private AccountNumber(String accountNumber) throws IllegalArgumentException {
        Objects.requireNonNull(accountNumber);
        if (!SIX_TO_EIGHT_ARABIC_NUMERALS.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException("Account number must consist of six to eight Arabic numerals inclusive e.g. \"12345678\"");
        }
        this.accountNumber = accountNumber;
    }

    public static AccountNumber of(String accountNumber) throws IllegalArgumentException {
        return new AccountNumber(accountNumber);
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == AccountNumber.class) {
            AccountNumber that = (AccountNumber) other;
            return this.accountNumber.equals(that.accountNumber);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return accountNumber.hashCode();
    }

    @Override
    public String toString() {
        return accountNumber;
    }

    public String getLastTwoDigits() {
        return accountNumber.substring(accountNumber.length() - 2);
    }

}
