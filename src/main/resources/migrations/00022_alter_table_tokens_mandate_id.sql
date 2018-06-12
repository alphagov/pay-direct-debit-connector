--liquibase formatted sql

--changeset uk.gov.pay:alter_table-tokens-mandate-id
ALTER TABLE tokens ADD COLUMN mandate_id BIGINT;
--rollback ALTER TABLE tokens DROP COLUMN mandate_id;

--changeset uk.gov.pay:tokens_mandates_fk
ALTER TABLE tokens ADD CONSTRAINT tokens_mandates_fk FOREIGN KEY (mandate_id) REFERENCES mandates (id);
--rollback drop constraint tokens_mandates_fk;

--changeset uk.gov.pay:alter_table-tokens-payment-requests-fk
ALTER TABLE tokens DROP CONSTRAINT tokens_payment_requests_fk
--rollback add constraint tokens_payment_requests_fk FOREIGN KEY (payment_request_id) REFERENCES payment_requests (id);
