package uk.gov.pay.directdebit.payments.dao;


import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.payments.dao.mapper.TokenMapper;

import java.util.Optional;

@RegisterMapper(TokenMapper.class)
public interface TokenDao {
    @SqlQuery("SELECT * FROM tokens t WHERE t.secure_redirect_token = :token")
    @SingleValueResult(Token.class)
    Optional<Token> findByTokenId(@Bind("token") String token);

    @SqlQuery("SELECT * FROM tokens t WHERE t.charge_id = :chargeId")
    @SingleValueResult(Token.class)
    Optional<Token> findByChargeId(@Bind("chargeId") Long chargeId);

    @SqlUpdate("INSERT INTO tokens(charge_id, secure_redirect_token) VALUES (:chargeId, :token)")
    @GetGeneratedKeys
    Long insert(@BindBean Token token);
}
