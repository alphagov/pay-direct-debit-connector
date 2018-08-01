package uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor;

import java.util.Objects;

public class GoCardlessServiceUserName {

    private final String goCardlessServiceUserName;

    private GoCardlessServiceUserName(String goCardlessServiceUserName) {
        this.goCardlessServiceUserName = Objects.requireNonNull(goCardlessServiceUserName);
    }

    public static GoCardlessServiceUserName of(String goCardlessServiceUserName) {
        return new GoCardlessServiceUserName(goCardlessServiceUserName);
    }

    public static GoCardlessServiceUserName valueOf(String goCardlessServiceUserName) {
        return GoCardlessServiceUserName.of(goCardlessServiceUserName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoCardlessServiceUserName that = (GoCardlessServiceUserName) o;

        return goCardlessServiceUserName.equals(that.goCardlessServiceUserName);
    }

    @Override
    public int hashCode() {
        return goCardlessServiceUserName.hashCode();
    }

    @Override
    public String toString() {
        return goCardlessServiceUserName;
    }
}
