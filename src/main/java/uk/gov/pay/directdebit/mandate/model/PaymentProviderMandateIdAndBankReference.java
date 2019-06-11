package uk.gov.pay.directdebit.mandate.model;

public class PaymentProviderMandateIdAndBankReference {

    private final PaymentProviderMandateId paymentProviderMandateId;
    private final MandateBankStatementReference mandateBankStatementReference;

    public PaymentProviderMandateIdAndBankReference(PaymentProviderMandateId paymentProviderMandateId, MandateBankStatementReference mandateBankStatementReference) {
        this.paymentProviderMandateId = paymentProviderMandateId;
        this.mandateBankStatementReference = mandateBankStatementReference;
    }

    public PaymentProviderMandateId getPaymentProviderMandateId() {
        return paymentProviderMandateId;
    }

    public MandateBankStatementReference getMandateBankStatementReference() {
        return mandateBankStatementReference;
    }
}
