package uk.gov.pay.directdebit.payments.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class SandboxPaymentIdArgumentFactory extends AbstractArgumentFactory<SandboxPaymentId> {

    public SandboxPaymentIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(SandboxPaymentId value, ConfigRegistry config) {
        String sandboxPaymentId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, sandboxPaymentId);
    }
}
