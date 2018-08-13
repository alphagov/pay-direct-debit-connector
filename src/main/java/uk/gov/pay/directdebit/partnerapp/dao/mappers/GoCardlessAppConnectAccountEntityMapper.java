package uk.gov.pay.directdebit.partnerapp.dao.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessAppConnectAccountEntityMapper implements RowMapper<GoCardlessAppConnectAccountEntity> {
    @Override
    public GoCardlessAppConnectAccountEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
        GoCardlessAppConnectAccountEntity goCardlessAppConnectAccountEntity = new GoCardlessAppConnectAccountEntity();
        goCardlessAppConnectAccountEntity.setId(rs.getLong("id"));
        goCardlessAppConnectAccountEntity.setToken(rs.getString("token"));
        goCardlessAppConnectAccountEntity.setGatewayAccountId(rs.getLong("gateway_account_id"));
        goCardlessAppConnectAccountEntity.setActive(rs.getBoolean("active"));
        goCardlessAppConnectAccountEntity.setRedirectUri(rs.getString("redirect_uri"));
        return goCardlessAppConnectAccountEntity;
    }
}
