--liquibase formatted sql

--changeset uk.gov.pay:00045_add_missing_go_cardless_event_attributes
ALTER TABLE gocardless_events DROP CONSTRAINT gocardless_events_events_fk;
ALTER TABLE gocardless_events DROP COLUMN event_id;
