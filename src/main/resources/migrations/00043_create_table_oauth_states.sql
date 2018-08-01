--liquibase formatted sql

--changeset uk.gov.pay:add_table-gocardless_partner_app_account_connect_tokens
CREATE TABLE gocardless_partner_app_account_connect_tokens (
  id BIGSERIAL PRIMARY KEY,
  gateway_account_id BIGSERIAL NOT NULL,
  token VARCHAR(26) NOT NULL,
  active BOOLEAN DEFAULT TRUE
);
--rollback drop table gocardless_partner_app_account_connect_tokens;

--changeset uk.gov.pay:add_gocardless_partner_app_account_connect_tokens_gateway_accounts_fk
ALTER TABLE gocardless_partner_app_account_connect_tokens ADD CONSTRAINT gocardless_partner_app_account_connect_tokens_gateway_accounts_fk FOREIGN KEY (gateway_account_id) REFERENCES gateway_accounts (id);
--rollback drop constraint gocardless_partner_app_account_connect_tokens_gateway_accounts_fk;