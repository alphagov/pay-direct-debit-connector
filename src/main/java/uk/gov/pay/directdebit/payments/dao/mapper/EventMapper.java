package uk.gov.pay.directdebit.payments.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.DirectDebitEvent;

public class EventMapper implements RowMapper<DirectDebitEvent> {
    private static final String ID_COLUMN = "id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    //TODO this will stay as transaction_id for the time being as the events table is likely to be replaced all-together.
    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String EVENT_TYPE_COLUMN = "event_type";
    private static final String EVENT_COLUMN = "event";
    private static final String EVENT_DATE_COLUMN = "event_date";
    private static final String MANDATE_EXTERNAL_ID_COLUMN = "mandate_external_id";
    private static final String TRANSACTION_EXTERNAL_ID_COLUMN = "payment_external_id";

    @Override
    public DirectDebitEvent map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new DirectDebitEvent(
                resultSet.getLong(ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                resultSet.getLong(MANDATE_ID_COLUMN),
                resultSet.getString(MANDATE_EXTERNAL_ID_COLUMN),
                resultSet.getLong(TRANSACTION_ID_COLUMN),
                resultSet.getString(TRANSACTION_EXTERNAL_ID_COLUMN),
                DirectDebitEvent.Type.valueOf(resultSet.getString(EVENT_TYPE_COLUMN)),
                DirectDebitEvent.SupportedEvent.valueOf(resultSet.getString(EVENT_COLUMN)),
                ZonedDateTime.ofInstant(resultSet.getTimestamp(EVENT_DATE_COLUMN).toInstant(), ZoneOffset.UTC));
    }
}
