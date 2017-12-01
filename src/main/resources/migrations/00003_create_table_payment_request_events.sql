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
