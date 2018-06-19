--liquibase formatted sql

--changeset uk.gov.pay:drop-table-payment-request-events
DROP TABLE payment_request_events;
--rollback CREATE TABLE payment_request_events