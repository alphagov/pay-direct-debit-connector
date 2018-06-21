--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-service_reference
ALTER TABLE mandates ADD COLUMN service_reference VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN service_reference;
