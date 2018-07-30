--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-mandates-add-creditor-id
ALTER TABLE gocardless_mandates ADD COLUMN gocardless_creditor_id VARCHAR(255);
--rollback ALTER TABLE gocardless_mandates DROP COLUMN gocardless_creditor_id;
