package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.mandate.model.Mandate;
import uk.gov.pay.directdebit.mandate.model.MandateState;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MandateMapper implements RowMapper<Mandate> {

    private static final String ID_COLUMN = "id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";
    private static final String PAYER_ID_COLUMN = "payer_id";
    private static final String STATE_COLUMN = "state";
    private static final String REFERENCE_COLUMN = "reference";

    @Override
    public Mandate map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Mandate(
                resultSet.getLong(ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                resultSet.getLong(PAYER_ID_COLUMN),
                resultSet.getString(REFERENCE_COLUMN),
                MandateState.valueOf(resultSet.getString(STATE_COLUMN))
        );
    }
}
