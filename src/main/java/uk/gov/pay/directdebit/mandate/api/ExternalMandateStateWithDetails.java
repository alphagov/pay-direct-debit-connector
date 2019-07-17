package uk.gov.pay.directdebit.mandate.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalMandateStateWithDetails {

    @JsonUnwrapped
    private final ExternalMandateState mandateState;

    private final String details;

    public ExternalMandateStateWithDetails(ExternalMandateState mandateState, String details) {
        this.mandateState = mandateState;
        this.details = details;
    }

    public ExternalMandateState getMandateState() {
        return mandateState;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ExternalPaymentStateWithDetails{" +
                "mandateState=" + mandateState +
                ", details='" + details + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ExternalMandateStateWithDetails that = (ExternalMandateStateWithDetails) other;
        return mandateState == that.mandateState && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mandateState, details);
    }
 
}
