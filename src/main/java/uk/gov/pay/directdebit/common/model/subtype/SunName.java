package uk.gov.pay.directdebit.common.model.subtype;

import java.util.Objects;

/**
 * The name associated with the Service User Number (SUN) registered with Bacs
 * For Direct Debit payments, this is what appears on the bank statement
 */
public class SunName {

    private final String sunName;

    private SunName(String sunName) {
        this.sunName = Objects.requireNonNull(sunName);
    }

    public static SunName of(String sunName) {
        return new SunName(sunName);
    }

    public static SunName valueOf(String sunName) {
        return SunName.of(sunName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SunName that = (SunName) o;

        return sunName.equals(that.sunName);
    }

    @Override
    public int hashCode() {
        return sunName.hashCode();
    }

    @Override
    public String toString() {
        return sunName;
    }
}
