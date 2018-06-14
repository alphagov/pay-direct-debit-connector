package uk.gov.pay.directdebit.payments.fixtures;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.fixtures.MandateFixture;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.Transaction;

public class TransactionFixture implements DbFixture<TransactionFixture, Transaction> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private MandateFixture mandateFixture = MandateFixture.aMandateFixture();
    private String externalId = RandomIdGenerator.newId();
    private Long amount = RandomUtils.nextLong(1, 99999);
    private PaymentState state = PaymentState.NEW;
    private String reference = RandomStringUtils.randomAlphanumeric(10);
    private String description = RandomStringUtils.randomAlphanumeric(20);
    private ZonedDateTime createdDate = ZonedDateTime.now(ZoneOffset.UTC);
    private TransactionFixture() {
    }

    public static TransactionFixture aTransactionFixture() {
        return new TransactionFixture();
    }

    public Long getId() {
        return id;
    }
    
    public TransactionFixture withId(Long id) {
        this.id = id;
        return this;
    }

    public MandateFixture getMandateFixture() {
        return mandateFixture;
    }

    public TransactionFixture withMandateFixture(
            MandateFixture mandateFixture) {
        this.mandateFixture = mandateFixture;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public TransactionFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Long getAmount() {
        return amount;
    }

    public TransactionFixture withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public PaymentState getState() {
        return state;
    }

    public TransactionFixture withState(PaymentState state) {
        this.state = state;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public TransactionFixture withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TransactionFixture withDescription(String description) {
        this.description = description;
        return this;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public TransactionFixture withCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    @Override
    public TransactionFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    transactions(\n" +
                                "        id,\n" +
                                "        mandate_id,\n" +
                                "        external_id,\n" +
                                "        amount,\n" +
                                "        state,\n" +
                                "        reference,\n" +
                                "        description,\n" +
                                "        type,\n" +
                                "        created_date\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)\n",
                        id,
                        mandateFixture.getId(),
                        externalId,
                        amount,
                        state,
                        reference,
                        description,
                        "charge",
                        createdDate
                )
        );
        return this;
    }

    @Override
    public Transaction toEntity() {
     
        return new Transaction(id, externalId, amount, state, description, reference, mandateFixture.toEntity(), createdDate);
    }

}
