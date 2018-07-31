--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-mandates-alter-column-gocardless-creditor-id-set-default
ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id SET DEFAULT 'LEGACY_CREDITOR_ID';
--rollback ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id DROP DEFAULT;

--changeset uk.gov.pay:alter_table-gocardless-mandates-update-gocardless-creditor-id
UPDATE gocardless_mandates SET gocardless_creditor_id = 'LEGACY_CREDITOR_ID' WHERE gocardless_creditor_id IS NULL;
--rollback UPDATE gocardless_mandates SET gocardless_creditor_id = NULL WHERE gocardless_creditor_id = 'LEGACY_CREDITOR_ID';

--changeset uk.gov.pay:alter_table-gocardless-mandates-alter-column-gocardless-creditor-id-set-not-null
ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id SET NOT NULL;
--rollback ALTER TABLE gocardless_mandates ALTER COLUMN gocardless_creditor_id DROP NOT NULL;
