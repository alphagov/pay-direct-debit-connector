package uk.gov.pay.directdebit.tokens.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.jdbi.v3.core.Jdbi;
import uk.gov.pay.directdebit.common.fixtures.DbFixture;
import uk.gov.pay.directdebit.payments.model.Token;

public class TokenFixture implements DbFixture<TokenFixture, Token> {
    private Long id = RandomUtils.nextLong(1, 99999);
    private String token = "3c9fee80-977a-4da5-a003-4872a8cf95b6";
    private Long mandateId = RandomUtils.nextLong(1, 99999);

    private TokenFixture() {
    }

    public static TokenFixture aTokenFixture() {
        return new TokenFixture();
    }

    public TokenFixture withMandateId(Long mandateId) {
        this.mandateId = mandateId;
        return this;
    }

    public TokenFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Long getMandateId() {
        return mandateId;
    }

    @Override
    public TokenFixture insert(Jdbi jdbi) {
        jdbi.withHandle(handle ->
                handle
                        .createUpdate("INSERT INTO tokens(mandate_id, secure_redirect_token) VALUES (:mandate_id, :secure_redirect_token)")
                        .bind("mandate_id", mandateId)
                        .bind("secure_redirect_token", token)
                        .execute()
        );
        return this;
    }

    @Override
    public Token toEntity() {
        return new Token(id, token, mandateId);
    }

}
