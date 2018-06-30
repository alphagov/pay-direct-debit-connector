package uk.gov.pay.directdebit.payers.model;

public class GoCardlessCustomer implements Entity {
    private Long id;
    private final Long payerId;
    private final String customerId;
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
    
    public GoCardlessCustomer setId(Long id) {
        this.id = id;
        return this;
    }

    public GoCardlessCustomer setCustomerBankAccountId(String customerBankAccountId) {
        this.customerBankAccountId = customerBankAccountId;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerBankAccountId() {
        return customerBankAccountId;
    }

    public Long getPayerId() {
        return payerId;
    }
}
