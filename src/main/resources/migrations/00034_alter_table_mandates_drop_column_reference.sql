--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-drop-reference
ALTER TABLE mandates DROP COLUMN reference;
--rollback ALTER TABLE mandates ADD COLUMN reference VARCHAR(255);
