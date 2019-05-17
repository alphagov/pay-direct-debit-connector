package uk.gov.pay.directdebit.payments.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class GoCardlessEventIdArgumentFactory extends AbstractArgumentFactory<GoCardlessEventId> {

    public GoCardlessEventIdArgumentFactory()  {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(GoCardlessEventId value, ConfigRegistry config) {
        String goCardlessEventId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, goCardlessEventId);
    }

}

