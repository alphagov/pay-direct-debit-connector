--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payers-payment-request-id
ALTER TABLE payers DROP COLUMN payment_request_id;
--rollback ADD COLUMN payment_request_id BIGINT NOT NULL;