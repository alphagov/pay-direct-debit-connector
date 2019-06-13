package uk.gov.pay.directdebit.payments.model;

/**
 * The ID of the payment used by Sandbox provider
 */
public class SandboxPaymentId extends PaymentProviderPaymentId {

    private SandboxPaymentId(String sandboxPaymentId) {
        super(sandboxPaymentId);
    }

    public static SandboxPaymentId valueOf(String sandboxPaymentId) {
        return new SandboxPaymentId(sandboxPaymentId);
    }

}
