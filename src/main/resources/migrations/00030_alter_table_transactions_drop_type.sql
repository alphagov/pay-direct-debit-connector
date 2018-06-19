--liquibase formatted sql

--changeset uk.gov.pay:alter_table-transactions-drop-type
ALTER TABLE transactions DROP COLUMN type;
--rollback ADD COLUMN type TEXT;
