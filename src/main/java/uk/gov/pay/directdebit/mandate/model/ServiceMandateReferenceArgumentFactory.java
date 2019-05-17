package uk.gov.pay.directdebit.mandate.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class ServiceMandateReferenceArgumentFactory extends AbstractArgumentFactory<ServiceMandateReference> {

    public ServiceMandateReferenceArgumentFactory()  {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(ServiceMandateReference value, ConfigRegistry config) {
        String serviceMandateReference = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, serviceMandateReference);
    }

}
