package uk.gov.pay.directdebit.payers.fixtures;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payers.model.Payer;

public class PayerFixture implements DbFixture<PayerFixture, Payer> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long mandateId = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private String name = RandomStringUtils.randomAlphabetic(7);
    private String email = generateEmail();
    private String sortCode = RandomStringUtils.randomNumeric(6);
    private String bankName = RandomStringUtils.randomAlphabetic(15);
    private String accountNumber = RandomStringUtils.randomNumeric(8);
    private String accountNumberLastTwoDigits = accountNumber.substring(accountNumber.length() - 2);
    private boolean accountRequiresAuthorisation = true;
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);

    private PayerFixture() {
    }

    public static PayerFixture aPayerFixture() {
        return new PayerFixture();
    }

    public PayerFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public PayerFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public PayerFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public PayerFixture withName(String name) {
        this.name = name;
        return this;
    }

    public PayerFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public PayerFixture withSortCode(String sortCode) {
        this.sortCode = sortCode;
        return this;
    }
    
    public PayerFixture withBankName(String bankName) {
        this.bankName = bankName;
        return this;
    }
    
    public PayerFixture withAccountNumberLastTwoDigits(String accountNumberLastTwoDigits) {
        this.accountNumberLastTwoDigits = accountNumberLastTwoDigits;
        return this;
    }

    public PayerFixture withAccountRequiresAuthorisation(boolean accountRequiresAuthorisation) {
        this.accountRequiresAuthorisation = accountRequiresAuthorisation;
        return this;
    }

    public PayerFixture withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public PayerFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getAccountNumberLastTwoDigits() {
        return accountNumberLastTwoDigits;
    }

    public boolean getAccountRequiresAuthorisation() {
        return accountRequiresAuthorisation;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public PayerFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    payers(\n" +
                                "        id,\n" +
                                "        mandate_id,\n" +
                                "        external_id,\n" +
                                "        name,\n" +
                                "        email,\n" +
                                "        bank_account_number_last_two_digits,\n" +
                                "        bank_account_requires_authorisation,\n" +
                                "        bank_account_number,\n" +
                                "        bank_account_sort_code,\n" +
                                "        bank_name,\n" +
                                "        created_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        mandateId,
                        externalId,
                        name,
                        email,
                        accountNumberLastTwoDigits,
                        accountRequiresAuthorisation,
                        accountNumber,
                        sortCode,
                        bankName,
                        Timestamp.from(createdDate.toInstant())
                )
        );
        return this;
    }

    @Override
    public Payer toEntity() {
        return new Payer(id, mandateId, externalId, name, email, sortCode, accountNumber, accountNumberLastTwoDigits, accountRequiresAuthorisation, bankName, createdDate);
    }

    private String generateEmail() {
        return RandomStringUtils.randomAlphanumeric(20) + "@" + RandomStringUtils.randomAlphanumeric(10) + ".test";
    }

}
