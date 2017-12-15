--liquibase formatted sql

--changeset uk.gov.pay:add_table-transactions
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_request_id BIGSERIAL NOT NULL,
    amount BIGINT NOT NULL,
    type TEXT NOT NULL,
    state TEXT NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table transactions;

--changeset uk.gov.pay:add_transactions_payment_requests_fk
ALTER TABLE transactions ADD CONSTRAINT transactions_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);
--rollback drop constraint transactions_payment_requests_fk;
