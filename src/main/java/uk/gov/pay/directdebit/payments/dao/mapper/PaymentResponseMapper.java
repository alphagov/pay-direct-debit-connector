package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.model.Payment;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.*;

public class PaymentResponseMapper implements RowMapper<PaymentResponse> {
    

    @Override
    public PaymentResponse map(ResultSet rs, StatementContext ctx) throws SQLException {
        return aPaymentResponse()
                .withState(
                        new ExternalPaymentStateWithDetails(
                                PaymentState.valueOf(rs.getString("state")).toExternal(), ""))
                .withAmount(rs.getLong("amount"))
                .withTransactionExternalId(rs.getString("payment_external_id"))
                .withDescription(rs.getString("description"))
                .withReference(rs.getString("reference"))
                .withCreatedDate(
                        ZonedDateTime.ofInstant(rs.getTimestamp("created_date").toInstant(), ZoneOffset.UTC))
                .build();
    }
}
