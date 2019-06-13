--liquibase formatted sql

--changeset uk.gov.pay:add_table_payments
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    amount BIGINT NOT NULL,
    state TEXT NOT NULL,
    version INTEGER DEFAULT 0 NOT NULL,
    mandate_id BIGINT,
    external_id VARCHAR(26),
    reference VARCHAR(255),
    description VARCHAR(255),
    created_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc'),
    payment_provider_id VARCHAR(255),
    charge_date TIMESTAMP WITH TIME ZONE
);
--rollback drop table payments;


--changeset uk.gov.pay:add_payments_mandates_fk
ALTER TABLE payments ADD CONSTRAINT payments_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint payments_mandates_fk;
