--liquibase formatted sql

--changeset uk.gov.pay:add_payment_requests_fk-gateway_accounts_id
ALTER TABLE payment_requests ADD CONSTRAINT fk_payment_requests_gateway_accounts FOREIGN KEY (gateway_account_id) REFERENCES gateway_accounts (id) ON DELETE NO ACTION ;

--rollback ALTER TABLE payment_requests DROP CONSTRAINT fk_payment_requests_gateway_accounts;
