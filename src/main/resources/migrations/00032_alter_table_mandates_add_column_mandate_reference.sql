--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-mandate_reference
ALTER TABLE mandates ADD COLUMN mandate_reference VARCHAR(18);
--rollback ALTER TABLE mandates DROP COLUMN mandate_reference;
