--liquibase formatted sql

--changeset uk.gov.pay:alter_table_gateway_accounts_add_access_token
ALTER TABLE gateway_accounts ADD COLUMN access_token VARCHAR(255);
ALTER TABLE gateway_accounts ADD UNIQUE (access_token);
--rollback ALTER TABLE gateway_accounts DROP COLUMN access_token;
