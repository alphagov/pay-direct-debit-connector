package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payments.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionMapper implements ResultSetMapper<Transaction> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String TYPE_COLUMN = "type";
    private static final String STATE_COLUMN = "state";

    @Override
    public Transaction map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Transaction(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN),
                resultSet.getLong(AMOUNT_COLUMN),
                Transaction.Type.valueOf(resultSet.getString(TYPE_COLUMN)),
                PaymentState.valueOf(resultSet.getString(STATE_COLUMN)));
    }
}
