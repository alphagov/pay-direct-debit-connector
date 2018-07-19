package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;

import javax.inject.Inject;
import java.util.Optional;

public class GatewayAccountCommandDao {

    private final Jdbi jdbi;
    private final String INSERT_QUERY = 
            "INSERT INTO gateway_accounts(external_id, payment_provider," + 
                    " type, service_name, description, analytics_id, access_token, organisation) " +
                    "VALUES (:externalId, :paymentProvider, " + 
                    ":type, :serviceName, :description, :analyticsId, :accessToken, :organisation)";
    private final String SELECT_ID_QUERY = "SELECT * from gateway_accounts t WHERE t.external_id = :externalId";
    
    @Inject
    public GatewayAccountCommandDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }
    
    public Optional<Long> insert(GatewayAccount gatewayAccount) {
        return jdbi.withHandle(handle -> {
            Update update = handle.createUpdate(INSERT_QUERY)
                    .bind("externalId", gatewayAccount.getExternalId())
                    .bind("paymentProvider", gatewayAccount.getPaymentProvider())
                    .bind("type", gatewayAccount.getType())
                    .bind("serviceName", gatewayAccount.getServiceName())
                    .bind("description", gatewayAccount.getDescription())
                    .bind("analyticsId", gatewayAccount.getAnalyticsId())
                    .bind("accessToken", gatewayAccount.getAccessToken().isPresent() ? 
                                                    gatewayAccount.getAccessToken().get().toString() : null)
                    .bind("organisation", gatewayAccount.getOrganisation().isPresent() ?
                                                    gatewayAccount.getOrganisation().get().toString() : null);
            update.execute();
            return handle.createQuery(SELECT_ID_QUERY)
                    .bind("externalId", gatewayAccount.getExternalId())
                    .mapTo(Long.class)
                    .findFirst();
        });
    }
}
