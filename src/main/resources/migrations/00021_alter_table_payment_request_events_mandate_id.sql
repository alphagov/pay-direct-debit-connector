--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payment-request-events-mandate-id
ALTER TABLE payment_request_events ADD COLUMN mandate_id BIGINT;
--rollback ALTER TABLE payment_request_events DROP COLUMN mandate_id;

--changeset uk.gov.pay:add_payment_request_events_mandates_fk
ALTER TABLE payment_request_events ADD CONSTRAINT payment_request_events_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint payment_request_events_mandates_fk;

--changeset uk.gov.pay:alter_table-payment_requests_events_payment_requests_fk
ALTER TABLE payment_request_events DROP CONSTRAINT payment_requests_events_payment_requests_fk
--rollback add constraint payment_requests_events_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);

--changeset uk.gov.pay:alter_table-payment-request-events-transaction-id
ALTER TABLE payment_request_events ADD COLUMN transaction_id BIGINT;
--rollback ALTER TABLE payment_request_events DROP COLUMN mandate_id;

--changeset uk.gov.pay:add_payment_request_events_transactions_fk
ALTER TABLE payment_request_events ADD CONSTRAINT payment_request_events_transactions_fk FOREIGN KEY (transaction_id) REFERENCES transactions (id);
--rollback drop constraint payment_request_events_transactions_fk;
