package uk.gov.pay.directdebit.payers.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;

public class Payer {
    private Long id;
    private Long mandateId;
    private String externalId;
    private String name;
    private String email;
    private String sortCode;
    private String accountNumber;
    private String bankName;
    private String accountNumberLastTwoDigits;
    private boolean accountRequiresAuthorisation;
    private ZonedDateTime createdDate;

    public Payer(Long id, Long mandateId, String externalId, String name, String email, String sortCode, String accountNumber, String accountNumberLastTwoDigits, boolean accountRequiresAuthorisation, String bankName, ZonedDateTime createdDate) {
        this.id = id;
        this.mandateId = mandateId;
        this.externalId = externalId;
        this.name = name;
        this.email = email;
        this.accountNumberLastTwoDigits = accountNumberLastTwoDigits;
        this.accountRequiresAuthorisation = accountRequiresAuthorisation;
        this.sortCode = sortCode;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.createdDate = createdDate;
    }

    public Payer(Long mandateId, String name, String email, String sortCode, String accountNumber, String accountNumberLastTwoDigits, String bankName, boolean accountRequiresAuthorisation) {
        this(null, mandateId, RandomIdGenerator.newId(), name, email, sortCode, accountNumber, accountNumberLastTwoDigits, accountRequiresAuthorisation, bankName, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getMandateId() {
        return mandateId;
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

    public String getBankName() {
        return bankName;
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
        if (!mandateId.equals(payer.mandateId)) {
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
        if (!bankName.equals(payer.bankName)) {
            return false;
        }
        return createdDate.equals(payer.createdDate);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + mandateId.hashCode();
        result = 31 * result + externalId.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + sortCode.hashCode();
        result = 31 * result + accountNumberLastTwoDigits.hashCode();
        result = 31 * result + (accountRequiresAuthorisation ? 1 : 0);
        result = 31 * result + accountNumber.hashCode();
        result = 31 * result + bankName.hashCode();
        result = 31 * result + createdDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Payer{" +
                "mandateId=" + mandateId +
                ", externalId='" + externalId + '\'' +
                '}';
    }
}
