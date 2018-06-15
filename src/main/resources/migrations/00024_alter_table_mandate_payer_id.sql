--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-payer-id
ALTER TABLE mandates DROP COLUMN payer_id;
--rollback ALTER TABLE mandates ADD COLUMN payer_id BIGINT NOT NULL;
