package uk.gov.pay.directdebit.payments.dao.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.pay.directdebit.payments.model.Token;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenMapper implements ResultSetMapper<Token> {
    private static final String ID_COLUMN = "id";
    private static final String CHARGE_ID_COLUMN = "charge_id";
    private static final String TOKEN_COLUMN = "secure_redirect_token";

    @Override
    public Token map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Token(resultSet.getLong(ID_COLUMN), resultSet.getString(TOKEN_COLUMN), resultSet.getLong(CHARGE_ID_COLUMN));
    }
}
