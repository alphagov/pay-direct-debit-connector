--liquibase formatted sql

--changeset uk.gov.pay:add_table-events
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    mandate_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    event_type TEXT NOT NULL,
    event TEXT NOT NULL,
    event_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table events;

--changeset uk.gov.pay:add_events_transactions_fk
ALTER TABLE events ADD CONSTRAINT events_transactions_fk FOREIGN KEY (transaction_id) REFERENCES transactions (id);
--rollback drop constraint events_transactions_fk;

--changeset uk.gov.pay:add_events_mandates_fk
ALTER TABLE events ADD CONSTRAINT events_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint events_mandates_fk;