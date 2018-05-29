--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payers-mandate-id
ALTER TABLE payers ADD COLUMN mandate_id BIGINT;
--rollback ALTER TABLE payers DROP COLUMN mandate_id;

--changeset uk.gov.pay:add_payers_mandates_fk
ALTER TABLE payers ADD CONSTRAINT payers_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint payers_mandates_fk;

--changeset uk.gov.pay:alter_table-payers-payment-requests-fk
ALTER TABLE payers DROP CONSTRAINT payers_payment_requests_fk
--rollback add constraint payers_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);

--changeset uk.gov.pay:alter_table-payers-payment-request-id
ALTER TABLE payers DROP COLUMN payment_request_id;
--rollback ADD COLUMN payment_request_id BIGINT NOT NULL;
