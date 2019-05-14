--liquibase formatted sql

--changeset uk.gov.pay:00045_add_missing_go_cardless_event_attributes
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