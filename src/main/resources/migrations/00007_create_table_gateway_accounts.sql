--liquibase formatted sql

--changeset uk.gov.pay:add_table-gateway_accounts
CREATE TABLE gateway_accounts (
    id BIGSERIAL PRIMARY KEY,
    payment_provider VARCHAR(255) NOT NULL,
    service_name VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    analytics_id VARCHAR(255),
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table gateway_accounts;
