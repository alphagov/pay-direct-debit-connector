--liquibase formatted sql

--changeset uk.gov.pay:add_table-gocardless_mandates
CREATE TABLE gocardless_mandates (
    id BIGSERIAL PRIMARY KEY,
    mandate_id BIGINT NOT NULL,
    gocardless_mandate_id VARCHAR(255) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table gocardless_mandates;

--changeset uk.gov.pay:add_mandates_gocardless_mandates_fk
ALTER TABLE gocardless_mandates ADD CONSTRAINT mandates_gocardless_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint mandates_gocardless_mandates_fk;
