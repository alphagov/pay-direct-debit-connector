package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventId;
import uk.gov.pay.directdebit.payments.model.GoCardlessResourceType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class GoCardlessEventMapper implements RowMapper<GoCardlessEvent> {
    private static final String ID_COLUMN = "id";
    private static final String EVENT_ID_COLUMN = "event_id";
    private static final String GOCARDLESS_EVENT_ID_COLUMN = "gocardless_event_id";
    private static final String ACTION_COLUMN = "action";
    private static final String RESOURCE_TYPE_COLUMN = "resource_type";
    private static final String JSON_COLUMN = "json";
    private static final String CREATED_AT_COLUMN = "created_at";

    @Override
    public GoCardlessEvent map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessEvent(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(EVENT_ID_COLUMN),
                GoCardlessEventId.valueOf(resultSet.getString(GOCARDLESS_EVENT_ID_COLUMN)),
                resultSet.getString(ACTION_COLUMN),
                GoCardlessResourceType.fromString(resultSet.getString(RESOURCE_TYPE_COLUMN)),
                resultSet.getString(JSON_COLUMN),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(CREATED_AT_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
