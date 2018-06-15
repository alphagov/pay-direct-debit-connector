package uk.gov.pay.directdebit.tokens.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.Token;

public class TokenMapper implements RowMapper<Token> {
    private static final String ID_COLUMN = "id";
    private static final String MANDATE_ID_COLUMN = "mandate_id";
    private static final String TOKEN_COLUMN = "secure_redirect_token";

    @Override
    public Token map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Token(resultSet.getLong(ID_COLUMN), resultSet.getString(TOKEN_COLUMN), resultSet.getLong(MANDATE_ID_COLUMN));
    }
}
