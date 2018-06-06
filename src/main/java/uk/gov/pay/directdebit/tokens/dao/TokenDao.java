package uk.gov.pay.directdebit.tokens.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.tokens.dao.mapper.TokenMapper;

@RegisterRowMapper(TokenMapper.class)
public interface TokenDao {
    @SqlQuery("SELECT * FROM tokens t WHERE t.secure_redirect_token = :token")
    Optional<Token> findByTokenId(@Bind("token") String token);

    @SqlUpdate("INSERT INTO tokens(mandate_id, secure_redirect_token) VALUES (:mandateId, :token)")
    @GetGeneratedKeys
    Long insert(@BindBean Token token);

    @SqlUpdate("DELETE FROM tokens t WHERE t.secure_redirect_token = :token")
    int deleteToken(@Bind("token") String token);
}
