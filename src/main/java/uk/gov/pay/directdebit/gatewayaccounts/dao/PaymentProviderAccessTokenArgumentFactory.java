package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;

import java.sql.Types;

public class PaymentProviderAccessTokenArgumentFactory extends AbstractArgumentFactory<PaymentProviderAccessToken> {
    
    public PaymentProviderAccessTokenArgumentFactory() {
        super(Types.VARCHAR);
    }
    
    @Override
    protected Argument build(PaymentProviderAccessToken value, ConfigRegistry config) {
        String externalId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, externalId);
    }

}
