package uk.gov.pay.directdebit.payments.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;

import java.sql.Types;

public class GoCardlessPaymentIdArgumentFactory extends AbstractArgumentFactory<GoCardlessPaymentId> {

    public GoCardlessPaymentIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(GoCardlessPaymentId value, ConfigRegistry config) {
        String goCardlessPaymentId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, goCardlessPaymentId);
    }
}
