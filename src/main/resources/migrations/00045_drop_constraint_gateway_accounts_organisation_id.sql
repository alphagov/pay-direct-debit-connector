--liquibase formatted sql

--changeset uk.gov.pay:alter_table_gateway_accounts_drop_constraint_gateway_accounts_organisation_key
ALTER TABLE gateway_accounts DROP CONSTRAINT IF EXISTS gateway_accounts_organisation_key;
--rollback ALTER TABLE gateway_accounts ADD UNIQUE (organisation);
