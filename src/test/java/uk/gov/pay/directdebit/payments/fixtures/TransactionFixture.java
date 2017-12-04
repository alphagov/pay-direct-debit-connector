package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.model.Transaction;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TransactionFixture implements DbFixture<TransactionFixture, Transaction> {
    private DBI jdbi;
    private Long id = RandomUtils.nextLong(1, 99999);
    private Long paymentRequestId = RandomUtils.nextLong(1, 99999);
    private Long amount = RandomUtils.nextLong(1, 99999);
    private Transaction.Type type = Transaction.Type.CHARGE;
    private PaymentState state = PaymentState.NEW;

    private TransactionFixture(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public static TransactionFixture transactionFixture(DBI jdbi) {
        return new TransactionFixture(jdbi);
    }

    public TransactionFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
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

    public Long getAmount() {
        return amount;
    }

    public Transaction.Type getType() {
        return type;
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public TransactionFixture insert() {
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
        return new Transaction(id, paymentRequestId, amount, type, state);
    }

}
