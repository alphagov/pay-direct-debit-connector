package uk.gov.pay.directdebit.partnerapp.dao.mappers;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.partnerapp.model.PartnerAppTokenEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PartnerAppTokenEntityMapper implements RowMapper<PartnerAppTokenEntity> {
    @Override
    public PartnerAppTokenEntity map(ResultSet rs, StatementContext ctx) throws SQLException {
        PartnerAppTokenEntity partnerAppTokenEntity = new PartnerAppTokenEntity();
        partnerAppTokenEntity.setId(rs.getLong("id"));
        partnerAppTokenEntity.setToken(rs.getString("token"));
        partnerAppTokenEntity.setGatewayAccountId(rs.getLong("gateway_account_id"));
        partnerAppTokenEntity.setActive(rs.getBoolean("active"));
        return partnerAppTokenEntity;
    }
}
