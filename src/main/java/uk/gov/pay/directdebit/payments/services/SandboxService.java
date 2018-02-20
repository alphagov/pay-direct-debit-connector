package uk.gov.pay.directdebit.payments.services;

import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.DirectDebitPaymentProvider;

public class SandboxService implements DirectDebitPaymentProvider {


    public SandboxService() {
    }


    @Override
    public String createCustomer(String paymentRequestExternalId, Payer payer, String sortCode, String accountNumber) {
        return null;
    }
}
