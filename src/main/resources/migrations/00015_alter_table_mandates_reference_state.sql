--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-reference
ALTER TABLE mandates ADD COLUMN reference VARCHAR(18);
--rollback ALTER TABLE mandates DROP COLUMN reference;

--changeset uk.gov.pay:alter_table-mandates-state
ALTER TABLE mandates ADD COLUMN state VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN state;
