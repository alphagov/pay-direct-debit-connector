--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-mandates-alter-column-gocardless-creditor-id-drop-default
ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id DROP DEFAULT;
--rollback ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id SET DEFAULT 'LEGACY_CREDITOR_ID';
