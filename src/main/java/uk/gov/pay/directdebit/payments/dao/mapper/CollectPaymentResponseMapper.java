package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.payments.api.CollectPaymentResponse;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.api.CollectPaymentResponse.CollectPaymentResponseBuilder.aCollectPaymentResponse;

public class CollectPaymentResponseMapper implements RowMapper<CollectPaymentResponse> {
    

    @Override
    public CollectPaymentResponse map(ResultSet rs, StatementContext ctx) throws SQLException {
        
        CollectPaymentResponse.CollectPaymentResponseBuilder collectPaymentResponse = aCollectPaymentResponse()
                .withState(
                        new ExternalPaymentStateWithDetails(
                                PaymentState.valueOf(rs.getString("state")).toExternal(), ""))
                .withAmount(rs.getLong("amount"))
                .withPaymentExternalId(rs.getString("payment_external_id"))
                .withDescription(rs.getString("description"))
                .withReference(rs.getString("reference"))
                .withCreatedDate(
                        ZonedDateTime.ofInstant(rs.getTimestamp("created_date").toInstant(), ZoneOffset.UTC))
                .withPaymentProvider(PaymentProvider.valueOf(rs.getString("payment_provider")));
        
        if(rs.getString("payment_provider").equalsIgnoreCase("gocardless")) {
            Optional.ofNullable(rs.getString("provider_id"))
                    .map(GoCardlessPaymentId::valueOf)
                    .ifPresent(collectPaymentResponse::withProviderId);
        } else {
            Optional.ofNullable(rs.getString("provider_id"))
                    .map(SandboxPaymentId::valueOf)
                    .ifPresent(collectPaymentResponse::withProviderId);
        }
        
        return collectPaymentResponse.build();
    }
}
