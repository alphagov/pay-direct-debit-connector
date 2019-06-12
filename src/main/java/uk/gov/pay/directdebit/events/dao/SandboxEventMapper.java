package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.events.model.SandboxEvent;
import uk.gov.pay.directdebit.mandate.model.SandboxMandateId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

public class SandboxEventMapper implements RowMapper<SandboxEvent> {

    @Override
    public SandboxEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
        var builder = SandboxEvent.SandboxEventBuilder.aSandboxEvent()
                .withCreatedAt(ZonedDateTime.parse(rs.getString("created_at")))
                .withEventCause(rs.getString("event_cause"))
                .withEventAction(rs.getString("event_action"))
                .withEventAction(rs.getString("payment_id"));

        if (rs.getString("mandate_id") == null) {
            builder.withMandateId(SandboxMandateId.valueOf(rs.getString("mandate_id")));
        }

        return builder.build();
    }
}
