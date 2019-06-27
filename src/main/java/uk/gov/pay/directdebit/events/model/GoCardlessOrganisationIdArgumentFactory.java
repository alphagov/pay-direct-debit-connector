package uk.gov.pay.directdebit.events.model;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;

import java.sql.Types;

public class GoCardlessOrganisationIdArgumentFactory extends AbstractArgumentFactory<GoCardlessOrganisationId> {

    public GoCardlessOrganisationIdArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(GoCardlessOrganisationId value, ConfigRegistry config) {
        String goCardlessOrganisationId = value == null ? null : value.toString();
        return (pos, stmt, context) -> stmt.setString(pos, goCardlessOrganisationId);
    }

}
