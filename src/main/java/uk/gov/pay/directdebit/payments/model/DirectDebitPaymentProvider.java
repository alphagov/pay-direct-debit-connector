package uk.gov.pay.directdebit.payments.model;

import uk.gov.pay.directdebit.payers.model.Payer;

public interface DirectDebitPaymentProvider {
    String createCustomer(String paymentRequestExternalId, Payer payer, String sortCode, String accountNumber);
}
