--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payment-requests
DROP TABLE payment_requests;
--rollback CREATE TABLE payment_requests;