package uk.gov.pay.directdebit.payments.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.PaymentState;
import uk.gov.pay.directdebit.payments.model.PaymentView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PaymentViewMapper implements RowMapper<PaymentView> {

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
    public PaymentView map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new PaymentView(
                rs.getString(GATEWAY_EXTERNAL_ACCOUNT_ID_COLUMN),
                rs.getString(PAYMENT_EXTERNAL_ID_COLUMN),
                rs.getLong(AMOUNT_COLUMN),
                rs.getString(REFERENCE_COLUMN),
                rs.getString(DESCRIPTION_COLUMN),
                ZonedDateTime.ofInstant(rs.getTimestamp(CREATED_DATE_COLUMN).toInstant(), ZoneOffset.UTC),
                rs.getString(NAME_COLUMN),
                rs.getString(EMAIL_COLUMN),
                PaymentState.valueOf(rs.getString(STATE_COLUMN)),
                rs.getString(MANDATE_EXTERNAL_ID_COLUMN)
                );
    }
}
