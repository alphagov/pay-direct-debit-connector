--liquibase formatted sql

--changeset uk.gov.pay:add_column-gocardless_partner_app_account_connect_tokens-add-return_uri
ALTER TABLE gocardless_partner_app_account_connect_tokens ADD COLUMN return_uri VARCHAR(255);
--rollback ALTER TABLE gocardless_partner_app_account_connect_tokens DROP COLUMN return_uri;
