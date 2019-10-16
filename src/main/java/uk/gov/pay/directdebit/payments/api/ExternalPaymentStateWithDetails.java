package uk.gov.pay.directdebit.payments.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalPaymentStateWithDetails {
    @JsonUnwrapped
    private final ExternalPaymentState paymentState;

    private final String details;

    public ExternalPaymentStateWithDetails(ExternalPaymentState paymentState, String details) {
        this.paymentState = paymentState;
        this.details = details;
    }

    public ExternalPaymentState getPaymentState() {
        return paymentState;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ExternalPaymentStateWithDetails{" +
                "paymentState=" + paymentState +
                ", details='" + details + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalPaymentStateWithDetails that = (ExternalPaymentStateWithDetails) o;
        return paymentState == that.paymentState &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentState, details);
    }
}
