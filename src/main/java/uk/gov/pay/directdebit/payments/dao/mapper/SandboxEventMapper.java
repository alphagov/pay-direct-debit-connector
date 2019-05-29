package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.SandboxEvent;

import org.jdbi.v3.core.mapper.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

public class SandboxEventMapper implements RowMapper<SandboxEvent> {

    private static final String CREATION_TIME_COLUMN = "creation_time";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    private static final String PAYMENT_ID_COLUMN = "payment_id";
    private static final String EVENT_ACTION_COLUMN = "event_action";
    private static final String EVENT_CAUSE_COLUMN = "event_cause";

    @Override
    public SandboxEvent map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new SandboxEvent(rs.getString(MANDATE_ID_COLUMN),
                rs.getString(PAYMENT_ID_COLUMN), rs.getString(EVENT_ACTION_COLUMN), rs.getString(EVENT_CAUSE_COLUMN),
                ZonedDateTime.parse(rs.getString(CREATION_TIME_COLUMN)));
    }
}
