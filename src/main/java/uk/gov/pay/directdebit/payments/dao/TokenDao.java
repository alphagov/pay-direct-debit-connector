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

    @SqlQuery("SELECT * FROM tokens t WHERE t.payment_request_id = :paymentRequestId")
    @SingleValueResult(Token.class)
    Optional<Token> findByPaymentId(@Bind("paymentRequestId") Long chargeId);

    @SqlUpdate("INSERT INTO tokens(payment_request_id, secure_redirect_token) VALUES (:paymentRequestId, :token)")
    @GetGeneratedKeys
    Long insert(@BindBean Token token);
}
