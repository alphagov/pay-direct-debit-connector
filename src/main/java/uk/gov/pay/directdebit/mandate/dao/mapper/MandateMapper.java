package uk.gov.pay.directdebit.mandate.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.mandate.model.Mandate;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MandateMapper implements ResultSetMapper<Mandate> {

    private static final String ID_COLUMN = "id";
    private static final String PAYER_ID_COLUMN = "payer_id";
    private static final String EXTERNAL_ID_COLUMN = "external_id";

    @Override
    public Mandate map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Mandate(
                resultSet.getLong(ID_COLUMN),
                resultSet.getString(EXTERNAL_ID_COLUMN),
                resultSet.getLong(PAYER_ID_COLUMN));
    }
}
