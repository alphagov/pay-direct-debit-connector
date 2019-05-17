package uk.gov.pay.directdebit.mandate.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.common.model.subtype.gocardless.creditor.GoCardlessCreditorId;
import uk.gov.pay.directdebit.common.util.RandomIdGenerator;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;

public class GoCardlessMandateFixture implements DbFixture<GoCardlessMandateFixture, GoCardlessMandate> {

    private Long id = RandomUtils.nextLong(1, 99999);
    private Long mandateId = RandomUtils.nextLong(1, 99999);
    private String goCardlessMandateId = RandomIdGenerator.newId();
    private GoCardlessCreditorId goCardlessCreditorId = GoCardlessCreditorId.valueOf(RandomIdGenerator.newId());

    private GoCardlessMandateFixture() {
    }

    public static GoCardlessMandateFixture aGoCardlessMandateFixture() {
        return new GoCardlessMandateFixture();
    }

    public Long getId() {
        return id;
    }

    public GoCardlessMandateFixture setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getMandateId() {
        return mandateId;
    }

    public GoCardlessMandateFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public String getGoCardlessMandateId() {
        return goCardlessMandateId;
    }

    public GoCardlessMandateFixture withGoCardlessMandateId(String goCardlessMandateId) {
        this.goCardlessMandateId = goCardlessMandateId;
        return this;
    }

    public GoCardlessMandateFixture withGoCardlessCreditorId(GoCardlessCreditorId goCardlessCreditorId) {
        this.goCardlessCreditorId = goCardlessCreditorId;
        return this;
    }

    @Override
    public GoCardlessMandateFixture insert(Jdbi jdbi) {
        jdbi.withHandle(h ->
                h.execute(
                        "INSERT INTO" +
                                "    gocardless_mandates(\n" +
                                "        id,\n" +
                                "        mandate_id,\n" +
                                "        gocardless_mandate_id,\n" +
                                "        gocardless_creditor_id\n" +
                                "    )\n" +
                                "   VALUES(?, ?, ?, ?)\n",
                        id,
                        mandateId,
                        goCardlessMandateId,
                        goCardlessCreditorId
                )
        );
        return this;
    }

    @Override
    public GoCardlessMandate toEntity() {
        return new GoCardlessMandate(id, mandateId, goCardlessMandateId, null, goCardlessCreditorId);
    }

}
