package uk.gov.pay.directdebit.mandate.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class PaymentProviderMandateIdArgumentFactory extends AbstractArgumentFactory<PaymentProviderMandateId> {

    public PaymentProviderMandateIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(PaymentProviderMandateId value, ConfigRegistry config) {
        String paymentProviderMandateId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, paymentProviderMandateId);
    }

}
