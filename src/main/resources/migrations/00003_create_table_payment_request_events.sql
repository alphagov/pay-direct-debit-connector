--liquibase formatted sql

--changeset uk.gov.pay:add_table-payment_request_events
CREATE TABLE payment_request_events (
    id BIGSERIAL PRIMARY KEY,
    payment_request_id BIGSERIAL NOT NULL,
    event_type TEXT NOT NULL,
    event TEXT NOT NULL,
    event_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table payment_request_events;

--changeset uk.gov.pay:add_payment_requests_events_payment_requests_fk
ALTER TABLE payment_request_events ADD CONSTRAINT payment_requests_events_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);
--rollback drop constraint payment_requests_events_payment_requests_fk;
