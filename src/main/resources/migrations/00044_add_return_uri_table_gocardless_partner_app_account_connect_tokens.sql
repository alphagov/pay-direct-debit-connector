--liquibase formatted sql

--changeset uk.gov.pay:add_column-gocardless_partner_app_account_connect_tokens-add-redirect_uri
ALTER TABLE gocardless_partner_app_account_connect_tokens ADD COLUMN redirect_uri VARCHAR(255);
--rollback ALTER TABLE gocardless_partner_app_account_connect_tokens DROP COLUMN redirect_uri;
