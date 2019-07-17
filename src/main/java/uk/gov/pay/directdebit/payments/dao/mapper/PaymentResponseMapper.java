package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.gatewayaccounts.model.PaymentProvider;
import uk.gov.pay.directdebit.mandate.model.subtype.MandateExternalId;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentId;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.SandboxPaymentId;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;

public class PaymentResponseMapper implements RowMapper<PaymentResponse> {
    

    @Override
    public PaymentResponse map(ResultSet rs, StatementContext ctx) throws SQLException {
        
        PaymentResponse.PaymentResponseBuilder paymentResponse = aPaymentResponse()
                .withState(
                        new ExternalPaymentStateWithDetails(
                                PaymentState.valueOf(rs.getString("state")).toExternal(), rs.getString("state_details")))
                .withAmount(rs.getLong("amount"))
                .withPaymentExternalId(rs.getString("payment_external_id"))
                .withDescription(rs.getString("description"))
                .withReference(rs.getString("reference"))
                .withMandateId(MandateExternalId.valueOf(rs.getString("mandate_external_id")))
                .withCreatedDate(
                        ZonedDateTime.ofInstant(rs.getTimestamp("created_date").toInstant(), ZoneOffset.UTC))
                .withPaymentProvider(PaymentProvider.valueOf(rs.getString("payment_provider")));
        
        if(rs.getString("payment_provider").equalsIgnoreCase("gocardless")) {
            Optional.ofNullable(rs.getString("provider_id"))
                    .map(GoCardlessPaymentId::valueOf)
                    .ifPresent(paymentResponse::withProviderId);
        } else {
            Optional.ofNullable(rs.getString("provider_id"))
                    .map(SandboxPaymentId::valueOf)
                    .ifPresent(paymentResponse::withProviderId);
        }
        
        return paymentResponse.build();
    }
}
