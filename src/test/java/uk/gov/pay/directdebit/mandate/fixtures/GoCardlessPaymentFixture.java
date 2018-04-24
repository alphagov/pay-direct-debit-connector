package uk.gov.pay.directdebit.mandate.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

import java.sql.Date;
import java.time.LocalDate;

public class GoCardlessPaymentFixture implements DbFixture<GoCardlessPaymentFixture, GoCardlessPayment> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private Long transactionId = RandomUtils.nextLong(1, 99999);
    private String paymentId = RandomIdGenerator.newId();
    private LocalDate chargeDate = LocalDate.now().plusDays(5);

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

    public LocalDate getChargeDate() {
        return chargeDate;
    }

    public GoCardlessPaymentFixture setChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
        return this;
    }

    @Override
    public GoCardlessPaymentFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    gocardless_payments(\n" +
                                "        id,\n" +
                                "        transaction_id,\n" +
                                "        payment_id,\n" +
                                "        charge_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?)\n",
                        id,
                        transactionId,
                        paymentId,
                        Date.valueOf(chargeDate)
                )
        );
        return this;
    }

    @Override
    public GoCardlessPayment toEntity() {
        return new GoCardlessPayment(id, transactionId, paymentId, chargeDate);
    }
}
