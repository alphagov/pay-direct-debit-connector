package uk.gov.pay.directdebit.common.dao;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;

public class DateArgumentFactory extends AbstractArgumentFactory<ZonedDateTime> {

    /**
     * Constructs an {@link ArgumentFactory} for type {@code T}.
     *
     * @param sqlType the {@link Types} constant to use when the argument value is {@code null}.
     */
    protected DateArgumentFactory(int sqlType) {
        super(sqlType);
    }

    @Override
    protected Argument build(ZonedDateTime zonedDateTime, ConfigRegistry config) {
        return (position, preparedStatement, statementContext1) ->
                preparedStatement.setTimestamp(position, Timestamp.from(zonedDateTime.toInstant()));
    }
}
