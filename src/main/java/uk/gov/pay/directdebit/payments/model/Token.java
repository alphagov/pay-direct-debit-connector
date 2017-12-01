package uk.gov.pay.directdebit.payments.model;

import java.util.UUID;

public class Token {

    private Long id;
    private String token;
    private Long chargeId;

    public Token(Long id, String token, Long chargeId) {
        this.id = id;
        this.token = token;
        this.chargeId = chargeId;
    }

    public Token(String token, Long chargeId) {
        this(null, token, chargeId);
    }
    public static Token generateNewTokenFor(Long chargeId) {
        return new Token(UUID.randomUUID().toString(), chargeId);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public void setChargeId(Long chargeId) {
        this.chargeId = chargeId;
    }
}
