package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentState;
import uk.gov.pay.directdebit.payments.api.ExternalPaymentStateWithDetails;
import uk.gov.pay.directdebit.payments.api.PaymentResponse;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static uk.gov.pay.directdebit.payments.api.PaymentResponse.PaymentResponseBuilder.aPaymentResponse;

public class PaymentResponseMapper implements RowMapper<PaymentResponse> {

    private static final String GATEWAY_EXTERNAL_ACCOUNT_ID_COLUMN = "gateway_external_id";
    private static final String PAYMENT_EXTERNAL_ID_COLUMN = "payment_external_id";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String REFERENCE_COLUMN = "reference";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String CREATED_DATE_COLUMN = "created_date";
    private static final String NAME_COLUMN = "name";
    private static final String EMAIL_COLUMN = "email";
    private static final String STATE_COLUMN = "state";
    private static final String MANDATE_EXTERNAL_ID_COLUMN = "mandate_external_id";

    @Override
    public PaymentResponse map(ResultSet rs, StatementContext ctx) throws SQLException {
        return  
                aPaymentResponse()
                        .withAmount(rs.getLong(AMOUNT_COLUMN))
                        .withDescription(rs.getString(DESCRIPTION_COLUMN))
                        .withReference(rs.getString(REFERENCE_COLUMN))
                        .withCreatedDate(
                                ZonedDateTime
                                        .ofInstant(rs.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC))
                        .withTransactionExternalId(rs.getString(PAYMENT_EXTERNAL_ID_COLUMN))
                        .withState(
                                new ExternalPaymentStateWithDetails(
                                        ExternalPaymentState.valueOf(rs.getString(STATE_COLUMN)),""))
                        .build();
    }
}
