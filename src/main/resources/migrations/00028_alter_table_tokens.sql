--liquibase formatted sql

--changeset uk.gov.pay:alter_table-tokens-request-id
ALTER TABLE tokens DROP COLUMN payment_request_id;
--rollback ADD COLUMN payment_request_id BIGINT NOT NULL;