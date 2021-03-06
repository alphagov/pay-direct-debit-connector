package uk.gov.pay.directdebit.gatewayaccounts.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GatewayAccountMapper implements RowMapper<GatewayAccount> {

    private static final String ID_COLUMN = "id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String PAYMENT_PROVIDER_COLUMN = "payment_provider";
    private static final String TYPE_COLUMN = "type";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String ANALYTICS_ID_COLUMN = "analytics_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ORGANISATION = "organisation";

    @Override
    public GatewayAccount map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        GatewayAccount gatewayAccount = new GatewayAccount(
                resultSet.getLong(ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                PaymentProvider.fromString(resultSet.getString(PAYMENT_PROVIDER_COLUMN)),
                GatewayAccount.Type.fromString(resultSet.getString(TYPE_COLUMN)),
                resultSet.getString(DESCRIPTION_COLUMN),
                resultSet.getString(ANALYTICS_ID_COLUMN)
        );
        
        String accessToken = resultSet.getString(ACCESS_TOKEN);
        if (accessToken != null) {
            gatewayAccount.setAccessToken(PaymentProviderAccessToken.of(accessToken));
        }
        
        String organisation = resultSet.getString(ORGANISATION);
        if (organisation != null) {
            gatewayAccount.setOrganisation(GoCardlessOrganisationId.valueOf(organisation));
        }
        
        return gatewayAccount;
    }
}
