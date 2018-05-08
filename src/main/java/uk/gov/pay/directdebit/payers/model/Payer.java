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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Payer payer = (Payer) o;

        if (accountRequiresAuthorisation != payer.accountRequiresAuthorisation) {
            return false;
        }
        if (id != null ? !id.equals(payer.id) : payer.id != null) {
            return false;
        }
        if (!paymentRequestId.equals(payer.paymentRequestId)) {
            return false;
        }
        if (!externalId.equals(payer.externalId)) {
            return false;
        }
        if (!name.equals(payer.name)) {
            return false;
        }
        if (!email.equals(payer.email)) {
            return false;
        }
        if (!sortCode.equals(payer.sortCode)) {
            return false;
        }
        if (!accountNumberLastTwoDigits.equals(payer.accountNumberLastTwoDigits)) {
            return false;
        }
        if (!accountNumber.equals(payer.accountNumber)) {
            return false;
        }
        if (addressLine1 != null ? !addressLine1.equals(payer.addressLine1)
                : payer.addressLine1 != null) {
            return false;
        }
        if (addressLine2 != null ? !addressLine2.equals(payer.addressLine2)
                : payer.addressLine2 != null) {
            return false;
        }
        if (addressPostcode != null ? !addressPostcode.equals(payer.addressPostcode)
                : payer.addressPostcode != null) {
            return false;
        }
        if (addressCity != null ? !addressCity.equals(payer.addressCity)
                : payer.addressCity != null) {
            return false;
        }
        if (addressCountry != null ? !addressCountry.equals(payer.addressCountry)
                : payer.addressCountry != null) {
            return false;
        }
        return createdDate.equals(payer.createdDate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + paymentRequestId.hashCode();
        result = 31 * result + externalId.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + sortCode.hashCode();
        result = 31 * result + accountNumberLastTwoDigits.hashCode();
        result = 31 * result + (accountRequiresAuthorisation ? 1 : 0);
        result = 31 * result + accountNumber.hashCode();
        result = 31 * result + (addressLine1 != null ? addressLine1.hashCode() : 0);
        result = 31 * result + (addressLine2 != null ? addressLine2.hashCode() : 0);
        result = 31 * result + (addressPostcode != null ? addressPostcode.hashCode() : 0);
        result = 31 * result + (addressCity != null ? addressCity.hashCode() : 0);
        result = 31 * result + (addressCountry != null ? addressCountry.hashCode() : 0);
        result = 31 * result + createdDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Payer{" +
                "paymentRequestId=" + paymentRequestId +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
