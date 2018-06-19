--liquibase formatted sql

--changeset uk.gov.pay:alter_table-transactions-payment-request-id
ALTER TABLE transactions DROP COLUMN payment_request_id;
--rollback ADD COLUMN payment_request_id BIGINT NOT NULL;

--changeset uk.gov.pay:alter_table-transactions-type
ALTER TABLE transactions ALTER COLUMN type DROP NOT NULL;
--rollback ALTER COLUMN type NOT NULL;
