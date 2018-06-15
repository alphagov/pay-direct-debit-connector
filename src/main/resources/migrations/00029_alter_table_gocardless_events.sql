--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-events-rename-event-id
ALTER TABLE gocardless_events RENAME COLUMN event_id TO gocardless_event_id;
--rollback ALTER TABLE gocardless_events RENAME COLUMN gocardless_event_id TO event_id;

--changeset uk.gov.pay:alter_table-gocardless-events-rename-payment-request-events-id
ALTER TABLE gocardless_events RENAME COLUMN payment_request_events_id TO event_id;
--rollback ALTER TABLE gocardless_events RENAME COLUMN event_id TO payment_request_events_id;