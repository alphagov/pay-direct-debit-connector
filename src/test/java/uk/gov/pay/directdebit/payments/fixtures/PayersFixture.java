package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payers.model.Payer;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PayersFixture implements DbFixture<PayersFixture, Payer> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private String name = "a name";
    private String email = "someone@mail.com";
    private String bankAccountNumberLastTwoDigits = String.valueOf(RandomUtils.nextLong(10, 99));
    private Boolean bankAccountRequiresAuthorisation = false;
    private String bankAccountNumber = "12345678";
    private String bankAccountSortCode = "123456";
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);

    public static PayersFixture aPayersFixture() { return new PayersFixture(); }

    public PayersFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public PayersFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public PayersFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public PayersFixture withName(String name) {
        this.name = name;
        return this;
    }

    public PayersFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public PayersFixture withBankAccountNumberLastTwoDigits(String bankAccountNumberLastTwoDigits) {
        this.bankAccountNumberLastTwoDigits = bankAccountNumberLastTwoDigits;
        return this;
    }

    public PayersFixture withBankAccountRequiresAuthorisation(Boolean bankAccountRequiresAuthorisation) {
        this.bankAccountRequiresAuthorisation = bankAccountRequiresAuthorisation;
        return this;
    }

    public PayersFixture withBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
        return this;
    }

    public PayersFixture withBankAccountSortCode(String bankAccountSortCode) {
        this.bankAccountSortCode = bankAccountSortCode;
        return this;
    }

    public PayersFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getId() { return id; }

    public Long getPaymentRequestId() { return paymentRequestId; }

    public String getExternalId() { return externalId; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getBankAccountNumberLastTwoDigits() { return bankAccountNumberLastTwoDigits; }

    public Boolean getBankAccountRequiresAuthorisation() { return bankAccountRequiresAuthorisation; }

    public String getBankAccountNumber() { return bankAccountNumber; }

    public String getBankAccountSortCode() { return bankAccountSortCode; }

    public ZonedDateTime getCreatedDate() { return createdDate; }

    @Override
    public PayersFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    payers(\n" +
                                "        id,\n" +
                                "        payment_request_id,\n" +
                                "        external_id,\n" +
                                "        name,\n" +
                                "        email,\n" +
                                "        bank_account_number_last_two_digits,\n" +
                                "        bank_account_requires_authorisation,\n" +
                                "        bank_account_number,\n" +
                                "        bank_account_sort_code,\n" +
                                "        created_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        paymentRequestId,
                        externalId,
                        name,
                        email,
                        bankAccountNumberLastTwoDigits,
                        bankAccountRequiresAuthorisation,
                        bankAccountNumber,
                        bankAccountSortCode,
                        Timestamp.from(createdDate.toInstant())
                )
        );
        return this;
    }

    @Override
    public Payer toEntity() {
        return new Payer(id, paymentRequestId, externalId, name, email, bankAccountSortCode, bankAccountNumber, bankAccountNumberLastTwoDigits,
                bankAccountRequiresAuthorisation, null, null, null, null, null, createdDate);
    }
}
