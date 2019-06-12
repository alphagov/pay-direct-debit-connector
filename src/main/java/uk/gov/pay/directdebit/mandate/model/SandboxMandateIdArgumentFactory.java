package uk.gov.pay.directdebit.mandate.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class SandboxMandateIdArgumentFactory extends AbstractArgumentFactory<SandboxMandateId> {

    public SandboxMandateIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(SandboxMandateId value, ConfigRegistry config) {
        String sandboxMandateId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, sandboxMandateId);
    }
}
