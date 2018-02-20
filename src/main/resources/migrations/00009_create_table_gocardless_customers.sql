--liquibase formatted sql

--changeset uk.gov.pay:add_table-gocardless_customers
CREATE TABLE gocardless_customers (
    id BIGSERIAL PRIMARY KEY,
    payer_id BIGINT NOT NULL,
    customer_id VARCHAR(255),
    customer_bank_account_id VARCHAR(255),
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table gocardless_customers;

--changeset uk.gov.pay:add_payers_gocardless_customers_fk
ALTER TABLE gocardless_customers ADD CONSTRAINT payers_gocardless_customers_fk FOREIGN KEY (payer_id) REFERENCES payers (id);
--rollback drop constraint payers_gocardless_customers_fk;
