package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.gatewayaccounts.dao.mapper.GatewayAccountMapper;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import java.util.List;
import java.util.Optional;

@RegisterMapper(GatewayAccountMapper.class)
public interface GatewayAccountDao {
    @SqlQuery("SELECT * FROM gateway_accounts p WHERE p.id = :id")
    @SingleValueResult(GatewayAccount.class)
    Optional<GatewayAccount> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM gateway_accounts")
    List<GatewayAccount> findAll();

    @SqlUpdate("INSERT INTO gateway_accounts(payment_provider, type, service_name, description, analytics_id) " +
            "VALUES (:paymentProvider, :type, :serviceName, :description, :analyticsId)")
    @GetGeneratedKeys
    Long insert(@BindBean GatewayAccount gatewayAccount);
}
