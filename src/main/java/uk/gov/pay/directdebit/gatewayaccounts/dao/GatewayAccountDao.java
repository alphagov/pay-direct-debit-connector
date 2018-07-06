package uk.gov.pay.directdebit.gatewayaccounts.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.gatewayaccounts.dao.mapper.GatewayAccountMapper;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import java.util.List;
import java.util.Optional;

@RegisterRowMapper(GatewayAccountMapper.class)
public interface GatewayAccountDao {
    @SqlQuery("SELECT * FROM gateway_accounts p WHERE p.id = :id")
    Optional<GatewayAccount> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM gateway_accounts p WHERE p.external_id = :externalId")
    Optional<GatewayAccount> findByExternalId(@Bind("externalId") String externalId);

    @SqlQuery("SELECT * FROM gateway_accounts")
    List<GatewayAccount> findAll();

    @SqlUpdate("INSERT INTO gateway_accounts(external_id, payment_provider, type, service_name, description, analytics_id) " +
            "VALUES (:externalId, :paymentProvider, :type, :serviceName, :description, :analyticsId)")
    @GetGeneratedKeys
    Long insert(@BindBean GatewayAccount gatewayAccount);

    @SqlQuery("SELECT * FROM gateway_accounts WHERE external_id IN (<externalAccountIds>)")
    List<GatewayAccount> find(
      @BindList("externalAccountIds") List<String> externalAccountIds
    );
}
