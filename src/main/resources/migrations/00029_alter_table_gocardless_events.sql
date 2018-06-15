--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-events-add-gocardless-event-id
ALTER TABLE gocardless_events ADD COLUMN gocardless_event_id VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN gocardless_event_id;
