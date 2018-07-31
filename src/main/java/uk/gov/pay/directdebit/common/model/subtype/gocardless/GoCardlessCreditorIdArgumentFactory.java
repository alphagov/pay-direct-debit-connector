package uk.gov.pay.directdebit.common.model.subtype.gocardless;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class GoCardlessCreditorIdArgumentFactory extends AbstractArgumentFactory<GoCardlessCreditorId> {

    public GoCardlessCreditorIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(GoCardlessCreditorId value, ConfigRegistry config) {
        String goCardlessCreditorId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, goCardlessCreditorId);
    }
}
