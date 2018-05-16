package uk.gov.pay.directdebit.payers.api;

import org.mindrot.jbcrypt.BCrypt;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.util.Map;

public class PayerParser {

    private String encrypt(String toEncrypt) {
        return BCrypt.hashpw(toEncrypt, BCrypt.gensalt(12));
    }

    public Payer parse(Map<String, String> createPayerMap, Transaction transaction) {
        String accountNumber = createPayerMap.get("account_number");
        String sortCode = createPayerMap.get("sort_code");
        String bankName = createPayerMap.getOrDefault("bank_name", null);
        return new Payer(
                transaction.getPaymentRequest().getId(),
                createPayerMap.get("account_holder_name"),
                createPayerMap.get("email"),
                encrypt(sortCode),
                encrypt(accountNumber),
                accountNumber.substring(accountNumber.length()-2),
                bankName,
                Boolean.parseBoolean(createPayerMap.get("requires_authorisation")));
    }
}
