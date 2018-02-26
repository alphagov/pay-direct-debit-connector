package uk.gov.pay.directdebit.mandate.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

public class GoCardlessPaymentFixture implements DbFixture<GoCardlessPaymentFixture, GoCardlessPayment> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private Long transactionId = RandomUtils.nextLong(1, 99999);
    private String paymentId = RandomIdGenerator.newId();

    private GoCardlessPaymentFixture() {

    }

    public static GoCardlessPaymentFixture aGoCardlessPaymentFixture() {
        return new GoCardlessPaymentFixture();
    }

    public Long getId() {
        return id;
    }

    public GoCardlessPaymentFixture setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public GoCardlessPaymentFixture withTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public GoCardlessPaymentFixture withPaymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    @Override
    public GoCardlessPaymentFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    gocardless_payments(\n" +
                                "        id,\n" +
                                "        transaction_id,\n" +
                                "        payment_id\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?)\n",
                        id,
                        transactionId,
                        paymentId
                )
        );
        return this;
    }

    @Override
    public GoCardlessPayment toEntity() {
        return new GoCardlessPayment(id, transactionId, paymentId);
    }
}
