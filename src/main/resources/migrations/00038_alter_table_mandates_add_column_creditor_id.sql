--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-creditor-id
ALTER TABLE mandates ADD COLUMN creditor_id VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN creditor_id;
