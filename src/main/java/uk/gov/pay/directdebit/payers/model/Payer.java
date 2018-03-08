package uk.gov.pay.directdebit.payers.model;

import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Payer {
    private Long id;
    private Long paymentRequestId;
    private String externalId;
    private String name;
    private String email;
    private String sortCode;
    private String accountNumberLastTwoDigits;
    private boolean accountRequiresAuthorisation;
    private String accountNumber;
    private String addressLine1;
    private String addressLine2;
    private String addressPostcode;
    private String addressCity;
    private String addressCountry;
    private ZonedDateTime createdDate;

    public Payer(Long id, Long paymentRequestId, String externalId, String name, String email, String sortCode, String accountNumber, String accountNumberLastTwoDigits, boolean accountRequiresAuthorisation, String addressLine1, String addressLine2, String addressPostcode, String addressCity, String addressCountry, ZonedDateTime createdDate) {
        this.id = id;
        this.paymentRequestId = paymentRequestId;
        this.externalId = externalId;
        this.name = name;
        this.email = email;
        this.accountNumberLastTwoDigits = accountNumberLastTwoDigits;
        this.accountRequiresAuthorisation = accountRequiresAuthorisation;
        this.sortCode = sortCode;
        this.accountNumber = accountNumber;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressPostcode = addressPostcode;
        this.addressCity = addressCity;
        this.addressCountry = addressCountry;
        this.createdDate = createdDate;
    }

    public Payer(Long paymentRequestId, String name, String email, String sortCode, String accountNumber, String accountNumberLastTwoDigits, boolean accountRequiresAuthorisation, String addressLine1, String addressLine2, String addressPostcode, String addressCity, String addressCountry) {
        this(null, paymentRequestId, RandomIdGenerator.newId(), name, email, sortCode, accountNumber, accountNumberLastTwoDigits, accountRequiresAuthorisation, addressLine1, addressLine2, addressPostcode, addressCity, addressCountry, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public void setId(Long id) {
        this.id = id;
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
}
