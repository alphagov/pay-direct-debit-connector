--liquibase formatted sql

--changeset uk.gov.pay:alter_table-mandates-return-url
ALTER TABLE mandates ADD COLUMN return_url VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN return_url;

--changeset uk.gov.pay:alter_table-mandates-gateway-account-id
ALTER TABLE mandates ADD COLUMN gateway_account_id BIGINT;
--rollback ALTER TABLE mandates DROP COLUMN gateway_account_id;

--changeset uk.gov.pay:add_mandates_fk-gateway_accounts_id
ALTER TABLE mandates ADD CONSTRAINT fk_mandates_gateway_accounts FOREIGN KEY (gateway_account_id) REFERENCES gateway_accounts (id) ON DELETE NO ACTION ;
--rollback ALTER TABLE mandates DROP CONSTRAINT fk_mandates_gateway_accounts;

--changeset uk.gov.pay:alter_table-mandates-type
ALTER TABLE mandates ADD COLUMN type VARCHAR(50);
--rollback ALTER TABLE mandates DROP COLUMN type;

--changeset uk.gov.pay:alter_table-mandates-created_date
ALTER TABLE mandates ADD COLUMN created_date TIMESTAMP WITH TIME ZONE DEFAULT (now() AT TIME ZONE 'utc') NOT NULL;
--rollback ALTER TABLE mandates DROP COLUMN created_date;

--changeset uk.gov.pay:alter_table-mandates-payer-id-fk
ALTER TABLE mandates DROP CONSTRAINT mandates_payers_fk
--rollback add constraint mandates_payers_fk FOREIGN KEY (payer_id) REFERENCES payers (id);

--changeset uk.gov.pay:alter_table-mandates-payer-id
ALTER TABLE mandates DROP COLUMN payer_id;
--rollback ALTER TABLE mandates ADD COLUMN payer_id BIGINT NOT NULL;
