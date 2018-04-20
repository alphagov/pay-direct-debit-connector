package uk.gov.pay.directdebit.tokens.dao.mapper;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.gov.pay.directdebit.payments.model.Token;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenMapper implements RowMapper<Token> {
    private static final String ID_COLUMN = "id";
    private static final String PAYMENT_REQUEST_ID_COLUMN = "payment_request_id";
    private static final String TOKEN_COLUMN = "secure_redirect_token";

    @Override
    public Token map(ResultSet resultSet, StatementContext statementContext) throws SQLException {
        return new Token(resultSet.getLong(ID_COLUMN), resultSet.getString(TOKEN_COLUMN), resultSet.getLong(PAYMENT_REQUEST_ID_COLUMN));
    }
}
