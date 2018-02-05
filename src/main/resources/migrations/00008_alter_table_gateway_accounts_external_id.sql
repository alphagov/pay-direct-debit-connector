--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gateway_accounts-external_id
ALTER TABLE gateway_accounts ALTER COLUMN external_id TYPE VARCHAR(255);
--rollback ALTER TABLE gateway_accounts ALTER COLUMN external_id TYPE VARCHAR(26);
