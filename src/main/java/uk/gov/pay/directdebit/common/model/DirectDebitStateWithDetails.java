package uk.gov.pay.directdebit.common.model;

import uk.gov.pay.directdebit.payments.model.DirectDebitState;

import java.util.Objects;
import java.util.Optional;

public class DirectDebitStateWithDetails<T extends DirectDebitState> {

    private final T state;
    private final String details;
    private final String detailsDescription;

    public DirectDebitStateWithDetails(T state, String details, String detailsDescription) {
        this.state = Objects.requireNonNull(state);
        this.details = details;
        this.detailsDescription = detailsDescription;
    }

    public T getState() {
        return state;
    }

    public Optional<String> getDetails() {
        return Optional.ofNullable(details);
    }

    public Optional<String> getDetailsDescription() {
        return Optional.ofNullable(detailsDescription);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DirectDebitStateWithDetails<?> that = (DirectDebitStateWithDetails<?>) other;
        return state.equals(that.state) &&
                Objects.equals(details, that.details) &&
                Objects.equals(detailsDescription, that.detailsDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, details, detailsDescription);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(state.toString());
        getDetails().ifPresent(details -> sb.append(" :").append(details));
        getDetailsDescription().ifPresent(details -> sb.append(" (").append(details).append(')'));
        return sb.toString();
    }

}
