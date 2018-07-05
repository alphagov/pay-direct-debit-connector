package uk.gov.pay.directdebit.mandate.model.subtype;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class MandateExternalIdArgumentFactory extends AbstractArgumentFactory<MandateExternalId> {

    public MandateExternalIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(MandateExternalId value, ConfigRegistry config) {
        String externalId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, externalId);
    }
}
