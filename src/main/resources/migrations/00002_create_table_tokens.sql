--liquibase formatted sql

--changeset uk.gov.pay:add_table-tokens
CREATE TABLE tokens (
    id BIGSERIAL PRIMARY KEY,
    charge_id BIGSERIAL NOT NULL,
    secure_redirect_token VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table tokens;
