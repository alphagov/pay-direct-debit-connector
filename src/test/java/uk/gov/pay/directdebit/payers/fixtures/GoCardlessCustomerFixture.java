package uk.gov.pay.directdebit.payers.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

public class GoCardlessCustomerFixture implements DbFixture<GoCardlessCustomerFixture, GoCardlessCustomer> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long payerId = RandomUtils.nextLong(1, 99999);
    private String customerId = RandomIdGenerator.newId();
    private String customerBankAccountId = RandomIdGenerator.newId();

    private GoCardlessCustomerFixture() {
    }

    public static GoCardlessCustomerFixture aGoCardlessCustomerFixture() {
        return new GoCardlessCustomerFixture();
    }

    public GoCardlessCustomerFixture withId(Long id) {
        this.id = id;
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

    public GoCardlessCustomerFixture withPayerId(Long payerId) {
        this.payerId = payerId;
        return this;
    }

    public GoCardlessCustomerFixture withCustomerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public GoCardlessCustomerFixture withCustomerBankAccountId(String customerBankAccountId) {
        this.customerBankAccountId = customerBankAccountId;
        return this;
    }

    @Override
    public GoCardlessCustomerFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    gocardless_customers(\n" +
                                "        id,\n" +
                                "        payer_id,\n" +
                                "        customer_id,\n" +
                                "        customer_bank_account_id)\n" +
                                "   VALUES(?, ?, ?, ?)\n",
                        id,
                        payerId,
                        customerId,
                        customerBankAccountId
                )
        );
        return this;
    }

    @Override
    public GoCardlessCustomer toEntity() {
        return new GoCardlessCustomer(id, payerId, customerId, customerBankAccountId);
    }

}
