--liquibase formatted sql

--changeset uk.gov.pay:00047_add_govukpay_events_table
CREATE TABLE govukpay_events;
ALTER TABLE govukpay_events ADD COLUMN id VARCHAR(255);
ALTER TABLE govukpay_events ADD COLUMN event_id VARCHAR(255);
ALTER TABLE govukpay_events ADD COLUMN action VARCHAR(255);
ALTER TABLE govukpay_events ADD COLUMN mandate_id VARCHAR(255);
ALTER TABLE govukpay_events ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL;
