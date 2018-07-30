--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-drop-creditor-id
ALTER TABLE mandates DROP COLUMN creditor_id;
--rollback ALTER TABLE mandates ADD COLUMN creditor_id VARCHAR(255);
