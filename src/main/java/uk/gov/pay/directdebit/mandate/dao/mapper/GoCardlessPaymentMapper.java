package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;
import uk.gov.pay.directdebit.mandate.model.GoCardlessPayment;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessPaymentMapper implements ResultSetMapper<GoCardlessPayment> {
    private static final String ID_COLUMN = "id";
    private static final String TRANSACTION_ID_COLUMN = "transaction_id";
    private static final String PAYMENT_ID_COLUMN = "payment_id";

    @Override
    public GoCardlessPayment map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessPayment(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(TRANSACTION_ID_COLUMN),
                resultSet.getString(PAYMENT_ID_COLUMN));
    }
}
