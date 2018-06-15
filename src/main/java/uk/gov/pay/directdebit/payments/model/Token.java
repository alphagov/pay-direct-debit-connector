package uk.gov.pay.directdebit.payments.model;

import java.util.UUID;

public class Token {

    private Long id;
    private String token;
    private Long mandateId;

    public Token(Long id, String token, Long mandateId) {
        this.id = id;
        this.token = token;
        this.mandateId = mandateId;
    }

    public Token(String token, Long paymentRequestId) {
        this(null, token, paymentRequestId);
    }
    public static Token generateNewTokenFor(Long paymentRequestId) {
        return new Token(UUID.randomUUID().toString(), paymentRequestId);
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getMandateId() {
        return mandateId;
    }

    public void setMandateId(Long mandateId) {
        this.mandateId = mandateId;
    }
}
