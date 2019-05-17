package uk.gov.pay.directdebit.mandate.model;

import java.util.Objects;

/**
 * The reference assigned to a mandate by a government service (set when the
 * service sends the request to us to create the mandate)
 */
public class ServiceMandateReference {

    private final String serviceMandateReference;

    private ServiceMandateReference(String serviceMandateReference) {
        this.serviceMandateReference = Objects.requireNonNull(serviceMandateReference);
    }

    public static ServiceMandateReference valueOf(String serviceMandateReference) {
        return new ServiceMandateReference(serviceMandateReference);
    }

    @Override
    public String toString() {
        return serviceMandateReference;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == ServiceMandateReference.class) {
            ServiceMandateReference that = (ServiceMandateReference) other;
            return this.serviceMandateReference.equals(that.serviceMandateReference);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return serviceMandateReference.hashCode();
    }

}
