--liquibase formatted sql

--changeset uk.gov.pay:add_table-gocardless_events
CREATE TABLE gocardless_events (
    id BIGSERIAL PRIMARY KEY,
    payment_request_events_id BIGINT,
    event_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255) NOT NULL,
    json jsonb NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table gocardless_events;

--changeset uk.gov.pay:add_payment_request_events_gocardless_events_fk
ALTER TABLE gocardless_events ADD CONSTRAINT payment_request_events_gocardless_events_fk FOREIGN KEY (payment_request_events_id) REFERENCES payment_request_events (id);
--rollback drop constraint payment_request_events_gocardless_events_fk;
