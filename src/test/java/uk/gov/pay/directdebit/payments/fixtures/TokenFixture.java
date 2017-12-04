package uk.gov.pay.directdebit.payments.fixtures;

import org.apache.commons.lang3.RandomUtils;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.directdebit.payments.model.Token;

public class TokenFixture implements DbFixture<TokenFixture, Token> {
    private DBI jdbi;
    private Long id = RandomUtils.nextLong(1, 99999);
    private String token = "3c9fee80-977a-4da5-a003-4872a8cf95b6";
    private Long paymentRequestId =  RandomUtils.nextLong(1, 99999);

    private TokenFixture( DBI jdbi) {
        this.jdbi = jdbi;
    }

    public static TokenFixture tokenFixture(DBI jdbi) {
        return new TokenFixture(jdbi);
    }

    public TokenFixture withPaymentRequestId(Long paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
        return this;
    }

    public TokenFixture withToken(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Long getPaymentRequestId() {
        return paymentRequestId;
    }

    @Override
    public TokenFixture insert() {
        jdbi.withHandle(handle ->
                handle
                        .createStatement("INSERT INTO tokens(payment_request_id, secure_redirect_token) VALUES (:payment_request_id, :secure_redirect_token)")
                        .bind("payment_request_id", paymentRequestId)
                        .bind("secure_redirect_token", token)
                        .execute()
        );
        return this;
    }

    @Override
    public Token toEntity() {
        return new Token(id, token, paymentRequestId);
    }

}
