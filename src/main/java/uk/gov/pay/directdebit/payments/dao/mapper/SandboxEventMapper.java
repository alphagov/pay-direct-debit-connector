package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.SandboxEvent;

import org.jdbi.v3.core.mapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

public class SandboxEventMapper implements RowMapper<SandboxEvent> {

    @Override
    public SandboxEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new SandboxEvent(
                rs.getString("mandate_id"),
                rs.getString("payment_id"),
                rs.getString("event_action"),
                rs.getString("event_cause"),
                ZonedDateTime.parse(rs.getString("created_at")));
    }
}
