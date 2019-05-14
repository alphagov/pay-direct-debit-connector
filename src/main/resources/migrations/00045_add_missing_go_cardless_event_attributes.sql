--liquibase formatted sql

--changeset 00045_add_missing_go_cardless_event_attributes
ALTER TABLE gocardless_events ADD COLUMN details_cause VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN details_description VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN details_origin VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN details_reason_code VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN details_scheme VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN mandate_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN customer_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN new_mandate_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN organisation_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN parent_event_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN payment_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN payout_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN previous_customer_bank_account_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN refund_id VARCHAR(255);
ALTER TABLE gocardless_events ADD COLUMN subscription_id VARCHAR(255);

--rollback ALTER TABLE gocardless_events DROP COLUMN details_cause, DROP COLUMN details_description, DROP COLUMN details_origin, DROP COLUMN details_reason_code, DROP COLUMN details_scheme, DROP COLUMN mandate_id, DROP COLUMN customer_id, DROP COLUMN new_mandate_id, DROP COLUMN organisation_id, DROP COLUMN parent_event_id, DROP COLUMN payment_id, DROP COLUMN payout_id, DROP COLUMN previous_customer_bank_account_id, DROP COLUMN refund_id, DROP COLUMN subscription_id;