package uk.gov.pay.directdebit.mandate.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class MandateBankStatementReferenceArgumentFactory extends AbstractArgumentFactory<MandateBankStatementReference> {

    public MandateBankStatementReferenceArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(MandateBankStatementReference value, ConfigRegistry config) {
        String mandateBankStatementReference = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, mandateBankStatementReference);
    }

}
