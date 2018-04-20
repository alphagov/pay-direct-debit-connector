package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessPaymentMapper implements RowMapper<GoCardlessPayment> {
    private static final String ID_COLUMN = "id";
    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String PAYMENT_ID_COLUMN = "payment_id";
    private static final String CHARGE_DATE_COLUMN = "charge_date";

    @Override
    public GoCardlessPayment map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessPayment(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(TRANSACTION_ID_COLUMN),
                resultSet.getString(PAYMENT_ID_COLUMN),
                resultSet.getDate(CHARGE_DATE_COLUMN).toLocalDate());
    }
}
