package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import uk.gov.pay.directdebit.payments.model.Token;
import uk.gov.pay.directdebit.util.DatabaseTestHelper;

public class TokenFixture implements DbFixture<TokenFixture, Token> {
    private DatabaseTestHelper databaseTestHelper;
    private Long id = RandomUtils.nextLong(1, 99999);
    private String token = "3c9fee80-977a-4da5-a003-4872a8cf95b6";
    private Long chargeId =  RandomUtils.nextLong(1, 99999);;

    private TokenFixture(DatabaseTestHelper databaseTestHelper) {
        this.databaseTestHelper = databaseTestHelper;
    }

    public static TokenFixture tokenFixture(DatabaseTestHelper databaseHelper) {
        return new TokenFixture(databaseHelper);
    }

    public TokenFixture withChargeId(Long chargeId) {
        this.chargeId = chargeId;
        return this;
    }

    public TokenFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Long getChargeId() {
        return chargeId;
    }

    @Override
    public TokenFixture insert() {
        databaseTestHelper.add(this);
        return this;
    }

    @Override
    public Token toEntity() {
        return new Token(id, token, chargeId);
    }

}
