--liquibase formatted sql

--changeset uk.gov.pay:add_table-govukpay_events
CREATE TABLE govukpay_events (
    id BIGSERIAL PRIMARY KEY,
    mandate_id BIGINT,
    payment_id BIGINT,
    event_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL,
    resource_type VARCHAR(32),
    event_type VARCHAR(255)
);
--rollback drop table govukpay_events;

--changeset uk.gov.pay:add_govukpay_events_mandates_fk
ALTER TABLE govukpay_events ADD CONSTRAINT govukpay_events_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint govukpay_events_mandates_fk;

--changeset uk.gov.pay:add_govukpay_events_payments_fk
ALTER TABLE govukpay_events ADD CONSTRAINT govukpay_events_payments_fk FOREIGN KEY (payment_id) REFERENCES payments (id);
--rollback drop constraint govukpay_events_payments_fk;