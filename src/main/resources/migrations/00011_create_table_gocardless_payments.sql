--liquibase formatted sql

--changeset uk.gov.pay:add_table-gocardless_payments
CREATE TABLE gocardless_payments (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    payment_id VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table gocardless_payments;

--changeset uk.gov.pay:add_transactions_gocardless_payments_fk
ALTER TABLE gocardless_payments ADD CONSTRAINT transactions_gocardless_payments_fk FOREIGN KEY (transaction_id) REFERENCES transactions (id);
--rollback drop constraint transactions_gocardless_payments_fk;
