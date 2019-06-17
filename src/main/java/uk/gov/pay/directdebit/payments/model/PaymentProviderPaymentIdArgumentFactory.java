package uk.gov.pay.directdebit.payments.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class PaymentProviderPaymentIdArgumentFactory extends AbstractArgumentFactory<PaymentProviderPaymentId> {

    public PaymentProviderPaymentIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(PaymentProviderPaymentId value, ConfigRegistry config) {
        String paymentProviderPaymentId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, paymentProviderPaymentId);
    }

}
