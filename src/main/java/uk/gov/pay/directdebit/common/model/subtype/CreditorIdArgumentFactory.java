package uk.gov.pay.directdebit.common.model.subtype;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class CreditorIdArgumentFactory extends AbstractArgumentFactory<CreditorId> {

    public CreditorIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(CreditorId value, ConfigRegistry config) {
        String creditorId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, creditorId);
    }
}
