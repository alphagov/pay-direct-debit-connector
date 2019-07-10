package uk.gov.pay.directdebit.mandate.model;

/**
 * The ID of the mandate used by Sandbox provider
 */
public class SandboxMandateId extends PaymentProviderMandateId implements MandateLookupKey {

    private SandboxMandateId(String sandboxMandateId) {
        super(sandboxMandateId);
    }

    public static SandboxMandateId valueOf(String sandBoxMandateId) {
        return new SandboxMandateId(sandBoxMandateId);
    }

}
