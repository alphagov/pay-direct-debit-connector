package uk.gov.pay.directdebit.mandate.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class GoCardlessMandateIdArgumentFactory extends AbstractArgumentFactory<GoCardlessMandateId> {

    public GoCardlessMandateIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(GoCardlessMandateId value, ConfigRegistry config) {
        String goCardlessMandateId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, goCardlessMandateId);
    }
}
