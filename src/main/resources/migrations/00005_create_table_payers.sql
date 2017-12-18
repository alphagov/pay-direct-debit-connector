--liquibase formatted sql

--changeset uk.gov.pay:add_table-payers
CREATE TABLE payers (
    id BIGSERIAL PRIMARY KEY,
    payment_request_id BIGSERIAL NOT NULL,
    external_id VARCHAR(26),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(254) NOT NULL,
    bank_account_number_last_two_digits VARCHAR(2) NOT NULL,
    bank_account_requires_authorisation BOOLEAN DEFAULT FALSE,
    bank_account_number TEXT NOT NULL,
    bank_account_sort_code TEXT NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    address_postcode VARCHAR(25) NOT NULL,
    address_city VARCHAR(255) NOT NULL,
    address_country VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table payers;

--changeset uk.gov.pay:add_payers_payment_requests_fk
ALTER TABLE payers ADD CONSTRAINT payers_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);
--rollback drop constraint payers_payment_requests_fk;

--changeset uk.gov.pay:add_index-payers_payment_external_id
CREATE INDEX payers_external_id_idx ON payers(external_id)
--rollback drop index payers_payment_external_id_idx;
