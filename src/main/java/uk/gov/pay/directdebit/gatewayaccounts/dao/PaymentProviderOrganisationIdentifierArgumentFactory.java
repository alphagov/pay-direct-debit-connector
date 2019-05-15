package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

import java.sql.Types;

public class PaymentProviderOrganisationIdentifierArgumentFactory extends AbstractArgumentFactory<PaymentProviderOrganisationIdentifier> {
    
    public PaymentProviderOrganisationIdentifierArgumentFactory() {
        super(Types.VARCHAR);
    }
    
    @Override
    protected Argument build(PaymentProviderOrganisationIdentifier value, ConfigRegistry config) {
        String paymentProviderOrganistationId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, paymentProviderOrganistationId);
    }

}
