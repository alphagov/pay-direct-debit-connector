package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessMandateMapper implements RowMapper<GoCardlessMandate> {
    private static final String ID_COLUMN = "id";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    private static final String GOCARDLESS_MANDATE_ID_COLUMN = "gocardless_mandate_id";

    @Override
    public GoCardlessMandate map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessMandate(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(MANDATE_ID_COLUMN),
                resultSet.getString(GOCARDLESS_MANDATE_ID_COLUMN));
    }
}
