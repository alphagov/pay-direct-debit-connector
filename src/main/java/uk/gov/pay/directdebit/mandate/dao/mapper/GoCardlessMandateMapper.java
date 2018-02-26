package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoCardlessMandateMapper implements ResultSetMapper<GoCardlessMandate> {
    private static final String ID_COLUMN = "id";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    private static final String GOCARDLESS_MANDATE_ID_COLUMN = "gocardless_mandate_id";

    @Override
    public GoCardlessMandate map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new GoCardlessMandate(
                resultSet.getLong(ID_COLUMN),
                resultSet.getLong(MANDATE_ID_COLUMN),
                resultSet.getString(GOCARDLESS_MANDATE_ID_COLUMN));
    }
}
