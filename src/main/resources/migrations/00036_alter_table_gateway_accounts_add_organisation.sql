--liquibase formatted sql

--changeset uk.gov.pay:alter_table_gateway_accounts_add_organisation
ALTER TABLE gateway_accounts ADD COLUMN organisation VARCHAR(255);
ALTER TABLE gateway_accounts ADD UNIQUE (organisation);
--rollback ALTER TABLE gateway_accounts DROP COLUMN organisation;
