package uk.gov.pay.directdebit.payers.api;

import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import uk.gov.pay.directdebit.payers.model.Payer;

public class PayerParser {

    private String encrypt(String toEncrypt) {
        return BCrypt.hashpw(toEncrypt, BCrypt.gensalt(12));
    }

    public Payer parse(Map<String, String> createPayerMap, Long mandateId) {
        String accountNumber = createPayerMap.get("account_number");
        String sortCode = createPayerMap.get("sort_code");
        String bankName = createPayerMap.getOrDefault("bank_name", null);
        return new Payer(
                mandateId,
                createPayerMap.get("account_holder_name"),
                createPayerMap.get("email"),
                encrypt(sortCode),
                encrypt(accountNumber),
                accountNumber.substring(accountNumber.length()-2),
                bankName,
                Boolean.parseBoolean(createPayerMap.get("requires_authorisation")));
    }
}
