--liquibase formatted sql

--changeset uk.gov.pay:add_table-mandates
CREATE TABLE mandates (
    id BIGSERIAL PRIMARY KEY,
    payer_id BIGSERIAL NOT NULL,
    external_id VARCHAR(30) NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table mandates;

--changeset uk.gov.pay:add_mandates_payers_fk
ALTER TABLE mandates ADD CONSTRAINT mandates_payers_fk FOREIGN KEY (payer_id) REFERENCES payers (id);
--rollback drop constraint mandates_payers_fk;

--changeset uk.gov.pay:add_mandates_external_id_idx
CREATE UNIQUE INDEX mandates_external_id_idx ON mandates(external_id)
--rollback drop index mandates_external_id_idx;
