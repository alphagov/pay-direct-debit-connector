package uk.gov.pay.directdebit.payers.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payers.model.GoCardlessCustomer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessCustomerMapper implements RowMapper<GoCardlessCustomer> {
    private static final String ID_COLUMN = "id";
    private static final String PAYER_ID_COLUMN = "payer_id";
    private static final String CUSTOMER_ID_COLUMN = "customer_id";
    private static final String CUSTOMER_BANK_ACCOUNT_ID_COLUMN = "customer_bank_account_id";

    @Override
    public GoCardlessCustomer map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessCustomer(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(PAYER_ID_COLUMN),
                resultSet.getString(CUSTOMER_ID_COLUMN),
                resultSet.getString(CUSTOMER_BANK_ACCOUNT_ID_COLUMN));
    }
}
