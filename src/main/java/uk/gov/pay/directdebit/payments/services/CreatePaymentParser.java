package uk.gov.pay.directdebit.payments.services;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.payments.model.PaymentStatesGraph;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class CreatePaymentParser {
    public Transaction parse(Map<String, String> createTransaction, Mandate mandate) {
        return new Transaction(
                new Long(createTransaction.get("amount")),
                PaymentStatesGraph.initialState(),
                createTransaction.get("description"),
                createTransaction.get("reference"),
                mandate,
                ZonedDateTime.now(ZoneOffset.UTC)
        );
    }
}
