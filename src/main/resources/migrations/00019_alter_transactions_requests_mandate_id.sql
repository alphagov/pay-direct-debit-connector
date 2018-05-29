--liquibase formatted sql

--changeset uk.gov.pay:alter_table-transactions-mandate-id
ALTER TABLE transactions ADD COLUMN mandate_id BIGINT;
--rollback ALTER TABLE transactions DROP COLUMN mandate_id;

--changeset uk.gov.pay:add_transactions_mandates_fk
ALTER TABLE transactions ADD CONSTRAINT transactions_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint transactions_mandates_fk;

--changeset uk.gov.pay:alter_table-payers-payment-requests-fk
ALTER TABLE transactions DROP CONSTRAINT transactions_payment_requests_fk
--rollback add constraint transactions_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);

--changeset uk.gov.pay:alter_table-transactions-payment-request-id
ALTER TABLE transactions DROP COLUMN payment_request_id;
--rollback ADD COLUMN payment_request_id BIGINT NOT NULL;

--changeset uk.gov.pay:alter_table-transactions-mandate-id
ALTER TABLE transactions ADD COLUMN mandate_id BIGINT;
--rollback ALTER TABLE transactions DROP COLUMN mandate_id;

--changeset uk.gov.pay:alter_table-transactions-external_id
ALTER TABLE transactions ADD COLUMN external_id VARCHAR(30);
--rollback ALTER TABLE transactions DROP COLUMN external_id;

--changeset uk.gov.pay:alter_table-transactions-reference
ALTER TABLE transactions ADD COLUMN reference VARCHAR(255);
--rollback ALTER TABLE transactions DROP COLUMN reference;

--changeset uk.gov.pay:alter_table-transactions-description
ALTER TABLE transactions ADD COLUMN description VARCHAR(255);
--rollback ALTER TABLE transactions DROP COLUMN description;

--changeset uk.gov.pay:alter_table-transactions-created-date
ALTER TABLE transactions ADD COLUMN created_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc');
--rollback ALTER TABLE transactions DROP COLUMN created_date;

--changeset uk.gov.pay:alter_table-transactions-type
ALTER TABLE transactions DROP COLUMN type;
--rollback ADD COLUMN type VARCHAR(40) NOT NULL;

