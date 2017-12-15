--liquibase formatted sql

--changeset uk.gov.pay:add_table-tokens
CREATE TABLE tokens (
    id BIGSERIAL PRIMARY KEY,
    payment_request_id BIGSERIAL NOT NULL,
    secure_redirect_token VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table tokens;

--changeset uk.gov.pay:add_tokens_payment_requests_fk
ALTER TABLE tokens ADD CONSTRAINT tokens_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);
--rollback drop constraint tokens_payment_requests_fk;

--changeset uk.gov.pay:add_index-tokens_secure_redirect_token
CREATE INDEX tokens_secure_redirect_token_idx ON tokens(secure_redirect_token)
--rollback drop index tokens_secure_redirect_token_idx;
