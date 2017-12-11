package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class TransactionFixture implements DbFixture<TransactionFixture, Transaction> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private String paymentRequestExternalId = RandomIdGenerator.newId();
    private Long amount = RandomUtils.nextLong(1, 99999);
    private Transaction.Type type = Transaction.Type.CHARGE;
    private PaymentState state = PaymentState.NEW;

    private TransactionFixture() {
    }

    public static TransactionFixture aTransactionFixture() {
        return new TransactionFixture();
    }

    public TransactionFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public TransactionFixture withExternalId(String paymentRequestExternalId) {
        this.paymentRequestExternalId = paymentRequestExternalId;
        return this;
    }

    public TransactionFixture withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public TransactionFixture withType(Transaction.Type type) {
        this.type = type;
        return this;
    }

    public TransactionFixture withState(PaymentState state) {
        this.state = state;
        return this;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    public String getPaymentRequestExternalId() {
        return paymentRequestExternalId;
    }

    public Long getAmount() {
        return amount;
    }

    public Transaction.Type getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public TransactionFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    transactions(\n" +
                                "        id,\n" +
                                "        payment_request_id,\n" +
                                "        amount,\n" +
                                "        type,\n" +
                                "        state\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?)\n",
                        id,
                        paymentRequestId,
                        amount,
                        type,
                        state
                )
        );
        return this;
    }

    @Override
    public Transaction toEntity() {
        return new Transaction(id, paymentRequestId, paymentRequestExternalId, amount, type, state);
    }

}
