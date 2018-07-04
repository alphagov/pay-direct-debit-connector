package uk.gov.pay.directdebit.payers.api;

import org.mindrot.jbcrypt.BCrypt;
import uk.gov.pay.directdebit.payers.model.AccountNumber;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payers.model.SortCode;

import java.util.Map;

public class PayerParser {

    private String encrypt(String toEncrypt) {
        return BCrypt.hashpw(toEncrypt, BCrypt.gensalt(12));
    }

    public Payer parse(Map<String, String> createPayerMap, Long mandateId) {
        AccountNumber accountNumber = AccountNumber.of(createPayerMap.get("account_number"));
        SortCode sortCode = SortCode.of(createPayerMap.get("sort_code"));
        String bankName = createPayerMap.getOrDefault("bank_name", null);
        return new Payer(
                mandateId,
                createPayerMap.get("account_holder_name"),
                createPayerMap.get("email"),
                encrypt(sortCode.toString()),
                encrypt(accountNumber.toString()),
                accountNumber.getLastTwoDigits(),
                bankName,
                Boolean.parseBoolean(createPayerMap.get("requires_authorisation")));
    }
}
