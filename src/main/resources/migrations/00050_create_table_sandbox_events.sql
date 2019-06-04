--liquibase formatted sql

--changeset uk.gov.pay:add_table-sandbox_events
CREATE TABLE sandbox_events (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    mandate_id VARCHAR(255),
    payment_id VARCHAR(255),
    event_action VARCHAR(255),
    event_cause VARCHAR(255)
);
--rollback drop table sandbox_events;