package uk.gov.pay.directdebit.common.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class DateArgumentFactory implements ArgumentFactory<ZonedDateTime> {
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx)
    {
        return value instanceof ZonedDateTime;
    }

    @Override
    public Argument build(Class<?> aClass, ZonedDateTime zonedDateTime, StatementContext statementContext) {
        return (position, preparedStatement, statementContext1) ->
                preparedStatement.setTimestamp(position, Timestamp.from(zonedDateTime.toInstant()));
    }
}
