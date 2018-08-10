package uk.gov.pay.directdebit.partnerapp.dao.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectTokenEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessAppConnectTokenEntityMapper implements RowMapper<GoCardlessAppConnectTokenEntity> {
    @Override
    public GoCardlessAppConnectTokenEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
        GoCardlessAppConnectTokenEntity goCardlessAppConnectTokenEntity = new GoCardlessAppConnectTokenEntity();
        goCardlessAppConnectTokenEntity.setId(rs.getLong("id"));
        goCardlessAppConnectTokenEntity.setToken(rs.getString("token"));
        goCardlessAppConnectTokenEntity.setGatewayAccountId(rs.getLong("gateway_account_id"));
        goCardlessAppConnectTokenEntity.setActive(rs.getBoolean("active"));
        goCardlessAppConnectTokenEntity.setRedirectUri(rs.getString("redirect_uri"));
        return goCardlessAppConnectTokenEntity;
    }
}
