package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GoCardlessEventMapper implements ResultSetMapper<GoCardlessEvent> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_EVENTS_ID_COLUMN = "payment_request_events_id";
    private static final String EVENT_ID_COLUMN = "event_id";
    private static final String ACTION_COLUMN = "action";
    private static final String RESOURCE_TYPE_COLUMN = "resource_type";
    private static final String JSON_COLUMN = "json";
    private static final String CREATED_AT_COLUMN = "created_at";

    @Override
    public GoCardlessEvent map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessEvent(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYMENT_REQUEST_EVENTS_ID_COLUMN),
                resultSet.getString(EVENT_ID_COLUMN),
                resultSet.getString(ACTION_COLUMN),
                resultSet.getString(RESOURCE_TYPE_COLUMN),
                resultSet.getString(JSON_COLUMN),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_AT_COLUMN).toInstant(), ZoneId.of("UTC")));
    }
}
