--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-events-internal-id
ALTER TABLE gocardless_events ADD COLUMN internal_event_id BIGINT;
--rollback DROP COLUMN internal_event_id;

--changeset uk.gov.pay:add_gocardless-events_events_fk
ALTER TABLE gocardless_events ADD CONSTRAINT gocardless_events_events_fk FOREIGN KEY (internal_event_id) REFERENCES events (id);
--rollback drop constraint transactions_mandates_fk;

--changeset uk.gov.pay:alter_table-gocardless-events-payment-request-events-fk
ALTER TABLE gocardless_events DROP CONSTRAINT payment_request_events_gocardless_events_fk
--rollback add constraint payment_request_events_gocardless_events_fk FOREIGN KEY (payment_request_events_id) REFERENCES events (id);