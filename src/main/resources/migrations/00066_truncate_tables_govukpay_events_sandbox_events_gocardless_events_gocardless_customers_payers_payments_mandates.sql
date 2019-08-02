--liquibase formatted sql

--changeset uk.gov.pay:delete_from_tokens
DELETE FROM tokens;

--changeset uk.gov.pay:delete_from_govukpay_events
DELETE FROM govukpay_events;

--changeset uk.gov.pay:delete_from_sandbox_events
DELETE FROM sandbox_events;

--changeset uk.gov.pay:delete_from_gocardless_events
DELETE FROM gocardless_events;

--changeset uk.gov.pay:delete_from_gocardless_customers
DELETE FROM gocardless_customers;

--changeset uk.gov.pay:delete_from__payers
DELETE FROM payers;

--changeset uk.gov.pay:delete_from__payments
DELETE FROM payments;

--changeset uk.gov.pay:delete_from__mandates
DELETE FROM mandates;
