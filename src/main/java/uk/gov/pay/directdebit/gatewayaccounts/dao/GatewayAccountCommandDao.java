package uk.gov.pay.directdebit.gatewayaccounts.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GatewayAccount;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderAccessToken;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProviderOrganisationIdentifier;

import javax.inject.Inject;
import java.util.Optional;

@RegisterArgumentFactory(PaymentProviderAccessTokenArgumentFactory.class)
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
            handle.registerArgument(new PaymentProviderAccessTokenArgumentFactory());
            handle.registerArgument(new PaymentProviderOrganisationIdentifierArgumentFactory());
            
            Update update = handle.createUpdate(INSERT_QUERY)
                    .bind("externalId", gatewayAccount.getExternalId())
                    .bind("paymentProvider", gatewayAccount.getPaymentProvider())
                    .bind("type", gatewayAccount.getType())
                    .bind("serviceName", gatewayAccount.getServiceName())
                    .bind("description", gatewayAccount.getDescription())
                    .bind("analyticsId", gatewayAccount.getAnalyticsId())
                    .bind("accessToken", gatewayAccount.getAccessToken())
                    .bind("organisation", gatewayAccount.getOrganisation());
            update.execute();
            return handle.createQuery(SELECT_ID_QUERY)
                    .bind("externalId", gatewayAccount.getExternalId())
                    .mapTo(Long.class)
                    .findFirst();
        });
    }
}
