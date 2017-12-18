package uk.gov.pay.directdebit.payers.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payers.model.Payer;
import uk.gov.pay.directdebit.payments.fixtures.TransactionFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PayerFixture implements DbFixture<PayerFixture, Payer> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private String name = RandomStringUtils.randomAlphabetic(7);
    private String email = generateEmail();
    private String sortCode = RandomStringUtils.randomNumeric(6);
    private String accountNumber = RandomStringUtils.randomNumeric(8);
    private String accountNumberLastTwoDigits = accountNumber.substring(accountNumber.length()-2);
    private boolean accountRequiresAuthorisation = true;
    private String addressLine1 = RandomStringUtils.randomAlphanumeric(10);
    private String addressLine2 = RandomStringUtils.randomAlphanumeric(10);
    private String addressPostcode = RandomStringUtils.randomAlphanumeric(6);
    private String addressCity = RandomStringUtils.randomAlphabetic(10);
    private String addressCountry = RandomStringUtils.randomAlphabetic(10);;
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneId.of("UTC"));

    private PayerFixture() { }

    public static PayerFixture aPayerFixture() {
        return new PayerFixture();
    }

    public PayerFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public PayerFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
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

    public PayerFixture withAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
        return this;
    }

    public PayerFixture withAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
        return this;
    }

    public PayerFixture withAddressPostcode(String addressPostcode) {
        this.addressPostcode = addressPostcode;
        return this;
    }

    public PayerFixture withAddressCity(String addressCity) {
        this.addressCity = addressCity;
        return this;
    }

    public PayerFixture withAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
        return this;
    }

    public PayerFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
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

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getAddressPostcode() {
        return addressPostcode;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public PayerFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
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
                                "        address_line1,\n" +
                                "        address_line2,\n" +
                                "        address_postcode,\n" +
                                "        address_city,\n" +
                                "        address_country,\n" +
                                "        created_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        paymentRequestId,
                        externalId,
                        name,
                        email,
                        accountNumberLastTwoDigits,
                        accountRequiresAuthorisation,
                        accountNumber,
                        sortCode,
                        addressLine1,
                        addressLine2,
                        addressPostcode,
                        addressCity,
                        addressCountry,
                        Timestamp.from(createdDate.toInstant())
                )
        );
        return this;
    }

    @Override
    public Payer toEntity() {
        return new Payer(id, paymentRequestId, externalId, name, email, sortCode, accountNumber, accountNumberLastTwoDigits, accountRequiresAuthorisation, addressLine1, addressLine2, addressPostcode, addressCity, addressCountry, createdDate);
    }

    private String generateEmail() {
        return RandomStringUtils.randomAlphanumeric(20) + "@" + RandomStringUtils.randomAlphanumeric(10) + ".com";
    }

}
