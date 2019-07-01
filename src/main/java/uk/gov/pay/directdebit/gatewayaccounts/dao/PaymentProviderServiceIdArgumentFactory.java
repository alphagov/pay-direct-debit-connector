package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderServiceId;

import java.sql.Types;

public class PaymentProviderServiceIdArgumentFactory extends AbstractArgumentFactory<PaymentProviderServiceId> {
    
    public PaymentProviderServiceIdArgumentFactory() {
        super(Types.VARCHAR);
    }
    
    @Override
    protected Argument build(PaymentProviderServiceId value, ConfigRegistry config) {
        String paymentProviderServiceId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, paymentProviderServiceId);
    }

}
