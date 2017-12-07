--liquibase formatted sql

--changeset uk.gov.pay:add_table-payment_requests
CREATE TABLE payment_requests (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(26),
    gateway_account_id BIGSERIAL NOT NULL,
    amount BIGINT NOT NULL,
    reference VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    return_url TEXT NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table payment_requests;

--changeset uk.gov.pay:add_index-payment_requests_external_id
CREATE UNIQUE INDEX payment_requests_external_id ON payment_requests(external_id)
--rollback drop index payment_requests_external_id;

