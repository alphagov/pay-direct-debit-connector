package uk.gov.pay.directdebit.payers.model;

public class GoCardlessCustomer {
    private Long id;
    private Long payerId;
    private String customerId;
    private String customerBankAccountId;

    public GoCardlessCustomer(Long id, Long payerId, String customerId, String customerBankAccountId) {
        this.id = id;
        this.payerId = payerId;
        this.customerId = customerId;
        this.customerBankAccountId = customerBankAccountId;
    }

    public GoCardlessCustomer(Long payerId, String customerId) {
        this(null, payerId, customerId, null);
    }

    public GoCardlessCustomer(Long payerId, String customerId, String customerBankAccountId) {
        this(null, payerId, customerId, customerBankAccountId);
    }

    public GoCardlessCustomer setId(Long id) {
        this.id = id;
        return this;
    }

    public GoCardlessCustomer setPayerId(Long payerId) {
        this.payerId = payerId;
        return this;
    }

    public GoCardlessCustomer setCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public GoCardlessCustomer setCustomerBankAccountId(String customerBankAccountId) {
        this.customerBankAccountId = customerBankAccountId;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Long getPayerId() {
        return payerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerBankAccountId() {
        return customerBankAccountId;
    }
}
