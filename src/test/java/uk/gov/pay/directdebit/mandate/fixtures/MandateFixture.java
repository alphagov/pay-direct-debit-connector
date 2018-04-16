package uk.gov.pay.directdebit.mandate.fixtures;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

public class MandateFixture implements DbFixture<MandateFixture, Mandate> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private Long payerId = RandomUtils.nextLong(1, 99999);
    private String externalId = RandomIdGenerator.newId();
    private String reference = RandomStringUtils.randomAlphanumeric(18);
    private MandateState state = MandateState.PENDING;

    private MandateFixture() {

    }

    public static MandateFixture aMandateFixture() {
        return new MandateFixture();
    }

    public Long getId() {
        return id;
    }

    public MandateFixture setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getPayerId() {
        return payerId;
    }

    public MandateFixture withPayerId(Long payerId) {
        this.payerId = payerId;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public MandateFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public MandateFixture withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public MandateState getState() {
        return state;
    }

    public MandateFixture withState(MandateState state) {
        this.state = state;
        return this;
    }

    @Override
    public MandateFixture insert(DBI jdbi) {
        jdbi.withHandle(h ->
                h.update(
                        "INSERT INTO" +
                                "    mandates(\n" +
                                "        id,\n" +
                                "        payer_id,\n" +
                                "        external_id,\n" +
                                "        reference,\n" +
                                "        state\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?, ?)\n",
                        id,
                        payerId,
                        externalId,
                        reference,
                        state.toString()
                )
        );
        return this;
    }

    @Override
    public Mandate toEntity() {
        return new Mandate(id, externalId, payerId, reference, state);
    }
}
