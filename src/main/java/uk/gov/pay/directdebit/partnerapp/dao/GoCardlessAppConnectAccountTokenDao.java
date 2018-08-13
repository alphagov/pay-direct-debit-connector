package uk.gov.pay.directdebit.partnerapp.dao;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.partnerapp.dao.mappers.GoCardlessAppConnectTokenEntityMapper;
import uk.gov.pay.directdebit.partnerapp.model.GoCardlessAppConnectAccountEntity;

import java.util.Optional;

@RegisterRowMapper(GoCardlessAppConnectTokenEntityMapper.class)
public interface GoCardlessAppConnectAccountTokenDao {

    @SqlUpdate("INSERT INTO gocardless_partner_app_account_connect_tokens(gateway_account_id, token, redirect_uri) VALUES (:gatewayAccountId, :token, :redirectUri)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessAppConnectAccountEntity token);

    @SqlQuery("SELECT * FROM gocardless_partner_app_account_connect_tokens g WHERE g.gateway_account_id = :gatewayAccountId AND g.active=TRUE")
    Optional<GoCardlessAppConnectAccountEntity> findByGatewayAccountId(@Bind("gatewayAccountId") Long gatewayAccountId);

    @SqlUpdate("UPDATE gocardless_partner_app_account_connect_tokens g SET active=FALSE WHERE token = :token AND gateway_account_id = :gatewayAccountId")
    Integer disableToken(@Bind("token") String token, @Bind("gatewayAccountId") Long gatewayAccountId);


    @SqlQuery("SELECT * FROM gocardless_partner_app_account_connect_tokens g WHERE g.token = :token AND g.gateway_account_id = :gatewayAccountId")
    Optional<GoCardlessAppConnectAccountEntity> findByTokenAndGatewayAccountId(@Bind("token") String token, @Bind("gatewayAccountId") Long gatewayAccountId);

    @SqlQuery("SELECT * FROM gocardless_partner_app_account_connect_tokens g WHERE g.token = :token AND g.active=TRUE")
    Optional<GoCardlessAppConnectAccountEntity> findActiveTokenByToken(@Bind("token") String token);

}
