--liquibase formatted sql

--changeset uk.gov.pay:add_table-mandates
CREATE TABLE mandates (
    id BIGSERIAL PRIMARY KEY,
    payer_id BIGSERIAL NOT NULL,
    external_id VARCHAR(30) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table mandates;
