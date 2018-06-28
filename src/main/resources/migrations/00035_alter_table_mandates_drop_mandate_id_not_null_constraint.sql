--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-drop-reference
ALTER TABLE gocardless_mandates ALTER gocardless_mandate_id DROP NOT NULL ;
ALTER TABLE gocardless_payments ALTER payment_id DROP NOT NULL ;
--rollback ALTER TABLE mandates ADD COLUMN reference VARCHAR(255);
