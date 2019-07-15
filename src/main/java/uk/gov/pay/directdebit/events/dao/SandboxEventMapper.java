package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

public class SandboxEventMapper implements RowMapper<SandboxEvent> {

    @Override
    public SandboxEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
        var builder = SandboxEvent.SandboxEventBuilder.aSandboxEvent()
                .withCreatedAt(ZonedDateTime.ofInstant(rs.getTimestamp("created_at").toInstant(), UTC))
                .withEventCause(rs.getString("event_cause"))
                .withEventAction(rs.getString("event_action"));

        if (rs.getString("mandate_id") != null) {
            builder.withMandateId(SandboxMandateId.valueOf(rs.getString("mandate_id")));
        }

        if (rs.getString("payment_id") != null) {
            builder.withPaymentId(SandboxPaymentId.valueOf(rs.getString("payment_id")));
        }

        return builder.build();
    }
}
